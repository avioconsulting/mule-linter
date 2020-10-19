pipeline {
	environment {
      VERSION = sh(returnStdout: true, script: "./gradlew properties -q | grep version: | awk '{print \$2}'").trim()
      IS_SNAPSHOT = VERSION.endsWith('SNAPSHOT')
      NEXUS_CREDENTIAL = 'nexus'
	}
	agent any
    tools {
        jdk 'jdk8'
    }
	stages {
		stage('Build') {
            stages {
                stage('Compile') {
                    steps {
                        script {
                            currentBuild.description = "v${VERSION}"
                        }
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
                stage('Deploy Artifact') {
                    steps {
                        withCredentials([usernamePassword(credentialsId: NEXUS_CREDENTIAL,
                            usernameVariable: 'MVN_USER',
                            passwordVariable: 'MVN_PASSWORD')]) {
                                withGradle {
                                    sh './gradlew publish'
                                }
                        }
                	}
                }
            }
		}

		stage('Release') {
            when {
                not {
                    environment name:'IS_SNAPSHOT', value:'true'
                }
            }
            stages {
                stage('Generate Release Notes') {
                    steps {
                        sh 'git checkout ${BRANCH_NAME}'
                        sh "echo '${VERSION}' >> CHANGELOG.md"
                        sh "git add CHANGELOG.md"
                        sh "git commit -m \"Adding v${VERSION} changelog\""
                    }
                }
                stage('Tag Release') {
                    steps {
                        sh "git tag -a v${VERSION} -m \"Version ${VERSION}\""
                        withCredentials([usernamePassword(credentialsId: scm.userRemoteConfigs.credentialsId[0], passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                            sh 'git config --local credential.helper "!f() { echo username=\\$GIT_USERNAME; echo password=\\$GIT_PASSWORD; }; f"'
                            sh 'git push --follow-tags'
                        }
                    }
                }
            }
		}

        stage('Increment SNAPSHOT Version') {
            when {
                not {
                    environment name:'IS_SNAPSHOT', value:'true'
                }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: scm.userRemoteConfigs.credentialsId[0], passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh 'git config --local credential.helper "!f() { echo username=\\$GIT_USERNAME; echo password=\\$GIT_PASSWORD; }; f"'
                    sh './gradlew incrementPatch -Dversion.prerelease=SNAPSHOT'
                    sh 'git add version.properties'
                    sh 'git commit -m "Incrementing to next SNAPSHOT patch version"'
                    sh 'git push'
                }
                script {
                    currentBuild.displayName = "v${VERSION}"
                }
            }
        }
    }
}