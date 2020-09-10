pipeline {
	environment {
	  MVN_SET = credentials('maven_secret_settings')
	  IS_RELEASE_TAG = sh(returnStdout: true, script: 'git tag --contains').trim().matches(/v\d{1,3}\.\d{1,3}\.\d{1,3}/)
// 	  IS_SNAPSHOT = readMavenPom(file: 'pom.xml').version.endsWith('SNAPSHOT')
      IS_SNAPSHOT = (new Properties(new File('gradle.properties').newInputStream())).get('version').endsWith('SNAPSHOT')
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
                	    echo 'mvn package -skipTests=true'
                	}
                }
                stage('Unit Test') {
                    steps {
                	    echo 'mvn test'
                	}
                }
                stage('Integration Test') {
                    steps {
                	    echo 'mvn integration-test'
                	}
                }
                stage('Deploy Artifact') {
                    steps {
                	    echo 'mvn deploy'
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