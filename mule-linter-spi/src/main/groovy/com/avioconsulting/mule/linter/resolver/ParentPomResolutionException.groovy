package com.avioconsulting.mule.linter.resolver

/**
 * Exception thrown when parent POM resolution fails.
 * Provides detailed context about what was attempted.
 */
class ParentPomResolutionException extends RuntimeException {
    
    final String failedCoordinates
    final String attemptedRelativePath
    final List<String> attemptedPaths
    final List<String> attemptedRepositories
    final File localRepositoryDir
    
    ParentPomResolutionException(String message,
                                  String failedCoordinates,
                                  String attemptedRelativePath,
                                  List<String> attemptedPaths,
                                  List<String> attemptedRepositories,
                                  File localRepositoryDir,
                                  Throwable cause) {
        super(buildMessage(message, failedCoordinates, attemptedRelativePath,
                          attemptedPaths, attemptedRepositories, localRepositoryDir), cause)
        this.failedCoordinates = failedCoordinates
        this.attemptedRelativePath = attemptedRelativePath
        this.attemptedPaths = attemptedPaths ?: []
        this.attemptedRepositories = attemptedRepositories ?: []
        this.localRepositoryDir = localRepositoryDir
    }
    
    private static String buildMessage(String message,
                                       String failedCoordinates,
                                       String attemptedRelativePath,
                                       List<String> attemptedPaths,
                                       List<String> attemptedRepositories,
                                       File localRepositoryDir) {
        StringBuilder sb = new StringBuilder()
        sb.append(message)

        if (failedCoordinates) {
            sb.append("\n  Failed coordinates: ").append(failedCoordinates)
        }

        if (attemptedRelativePath) {
            sb.append("\n  Attempted relativePath: ").append(attemptedRelativePath)
            if (attemptedPaths && !attemptedPaths.isEmpty()) {
                File attemptedFile = new File(attemptedPaths[0])
                sb.append(" (file ").append(attemptedFile.exists() ? "exists" : "not found").append(")")
            }
        }

        if (attemptedRepositories && !attemptedRepositories.isEmpty()) {
            sb.append("\n  Attempted repositories:")
            attemptedRepositories.each { repo ->
                sb.append("\n    - ").append(repo)
            }
        }

        sb.append("\n  Local repository: ").append(
            localRepositoryDir?.absolutePath ?: "${System.getProperty('user.home')}/.m2/repository")

        return sb.toString()
    }
    
    /**
     * Returns true if the failure was due to authentication
     */
    boolean isAuthenticationFailure() {
        return cause?.class?.name?.contains('AuthenticationException') ||
               message?.contains('401') ||
               message?.contains('403')
    }
    
    /**
     * Returns true if the failure was due to network/connectivity
     */
    boolean isNetworkFailure() {
        return cause?.class?.name?.contains('TransferException') ||
               cause?.class?.name?.contains('ConnectException') ||
               message?.contains('Connection refused') ||
               message?.contains('UnknownHost')
    }
    
    /**
     * Returns true if the parent was not found (404)
     */
    boolean isNotFound() {
        return message?.contains('404') ||
               (cause?.message?.contains('404'))
    }
}
