package com.avioconsulting.mule.linter.resolver

import org.apache.maven.settings.Settings
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory
import org.apache.maven.settings.building.SettingsBuildingException
import org.apache.maven.settings.building.SettingsBuildingRequest
import org.apache.maven.settings.building.SettingsBuildingResult

/**
 * Parses Maven settings.xml files from standard locations.
 * Supports both user settings (~/.m2/settings.xml) and global settings (M2_HOME/conf/settings.xml).
 * 
 * Currently supported settings:
 * - Local repository path (settings.localRepository)
 * - Server authentication (settings.servers)
 * 
 * Note: Profile repositories, mirrors, and proxies are parsed but not currently
 * used by ParentPomResolver (planned for future profile support).
 */
class SettingsXmlParser {
    
    private static final String USER_SETTINGS_PATH = "${System.getProperty('user.home')}/.m2/settings.xml"
    private static final String GLOBAL_SETTINGS_PATH = "${System.getenv('M2_HOME')}/conf/settings.xml"
    
    /**
     * Loads Maven settings from standard locations.
     * First tries user settings, then global settings.
     * Returns empty settings if neither exists.
     * 
     * @return Settings object with merged user and global settings
     */
    Settings loadSettings() {
        File userSettingsFile = new File(USER_SETTINGS_PATH)
        File globalSettingsFile = System.getenv('M2_HOME') ? 
            new File(GLOBAL_SETTINGS_PATH) : null
        
        def builder = new DefaultSettingsBuilderFactory().newInstance()
        SettingsBuildingRequest request = new org.apache.maven.settings.building.DefaultSettingsBuildingRequest()
        
        if (userSettingsFile.exists()) {
            request.setUserSettingsFile(userSettingsFile)
        }
        
        if (globalSettingsFile?.exists()) {
            request.setGlobalSettingsFile(globalSettingsFile)
        }
        
        try {
            SettingsBuildingResult result = builder.build(request)
            return result.getEffectiveSettings()
        } catch (SettingsBuildingException e) {
            // Log warning and return empty settings
            System.err.println("Warning: Failed to parse settings.xml: ${e.message}")
            return new Settings()
        }
    }
    
    /**
     * Gets the local repository path from settings, or returns default.
     * @param settings The settings object
     * @return Local repository directory path
     */
    String getLocalRepositoryPath(Settings settings) {
        return settings?.localRepository ?: 
            "${System.getProperty('user.home')}/.m2/repository"
    }
    
    /**
     * Finds server authentication for a given repository ID.
     * @param settings The settings object
     * @param serverId The server/repository ID
     * @return Server settings with username/password, or null if not found
     */
    org.apache.maven.settings.Server getServerAuthentication(Settings settings, String serverId) {
        return settings?.servers?.find { it.id == serverId }
    }
    
    /**
     * Gets active proxy configuration from settings.
     * @param settings The settings object
     * @return Proxy settings, or null if no active proxy
     */
    org.apache.maven.settings.Proxy getActiveProxy(Settings settings) {
        return settings?.proxies?.find { it.active }
    }
    
    /**
     * Checks if settings.xml was loaded successfully.
     * @param settings The settings object
     * @return true if at least one settings file was parsed with usable configuration
     */
    boolean hasSettings(Settings settings) {
        return settings != null && 
               (settings.localRepository ||
                settings.servers?.size() > 0 ||
                settings.proxies?.size() > 0 ||
                settings.mirrors?.size() > 0)
    }
}
