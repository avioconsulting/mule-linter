pipeline {
	environment {
	  MVN_SET = credentials('maven_secret_settings')
	  IS_RELEASE_TAG = sh(returnStdout: true, script: 'git tag --contains').trim().matches(/v\d{1,3}\.\d{1,3}\.\d{1,3}/)
      IS_SNAPSHOT = sh(returnStdout: true, script: "./gradlew properties -q | grep version: | awk '{print \$2}'").trim().endsWith('SNAPSHOT')
	}
	agent any
    tools {
        maven 'Maven 3'
        jdk 'jdk8'
    }
	stages {
		stage('Build') {
            when {
                not {
                    environment name:'IS_RELEASE_TAG', value:'true'
                }
            }
            stages {
                stage('Compile') {
                    steps {
                        withGradle {
                	        sh './gradlew classes'
                	    }
                	}
                }
                stage('Validate') {
                    steps {
                        withGradle {
                            sh './gradlew check -x test'
                        }
                    }
                }
                stage('Unit Test') {
                    steps {
                        withGradle {
                            sh './gradlew test'
                        }
                	}
                }
                stage('Integration Test') {
                    steps {
                	    echo 'NO INTEGRATION TESTS - ./gradlew integration-test'
                	}
                }
                stage('Deploy Artifact') {
                    steps {
                        withCredentials([usernamePassword(credentialsId: 'nexus',
                            usernameVariable: 'ORG_GRADLE_PROJECT_MVN_USER',
                            passwordVariable: 'ORG_GRADLE_PROJECT_MVN_PASSWORD')]) {
                                sh 'export'
                                sh './gradlew publish'
                        }
                	}
                }
            }
		}

		stage('Release') {
            when {
                allOf {
                    not {
                        environment name:'IS_RELEASE_TAG', value:'true'
                    }
                    not {
                        environment name:'IS_SNAPSHOT', value:'true'
                    }
                }
            }
            stages {
                stage('Generate Release Notes') {
                    steps {
                        echo 'Generate release notes'
                    }
                }
                stage('Tag Release') {
                    steps {
                        echo 'git tag release'
                    }
                }
            }
		}

        stage('Increment SNAPSHOT Version') {
            when {
                environment name:'IS_RELEASE_TAG', value:'true'
            }
            steps {
                    echo 'Incrementing SNAPSHOT version'
                    echo 'git add pom.xml, commit, push'
            }
        }
    }
}