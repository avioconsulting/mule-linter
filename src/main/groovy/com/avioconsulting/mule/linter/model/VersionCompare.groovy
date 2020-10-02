package com.avioconsulting.mule.linter.model

class VersionCompare {

    enum VersionOperator {

        EQUAL,
        GRATER_THAN

    }

    /**
     * It checks baseVersion with given version for given operator.
     * @param baseVersion the version String to be compared against.
     * @param version the version String to be compared with baseVersion.
     * @param operator the operator for version to be satisfied.
     * @return true if specified version validate baseVersion with given operator; false otherwise.
     */
    Boolean isValidVersion(String baseVersion, String version, VersionOperator operator) {
        switch (operator) {
            case VersionOperator.EQUAL:
                return baseVersion == version
            case VersionOperator.GRATER_THAN:
                return baseVersion < version
        }
    }
}

