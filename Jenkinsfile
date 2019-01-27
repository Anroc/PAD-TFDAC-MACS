// http://your.jenkins.host/scriptApproval/
import groovy.json.JsonOutput

DOCUMENT_NAME = "multi-authority-abe"
DOCUMENTATION_DIR = "./thesis"
SOURCE_DIR = "./implementation/tfdacmacs"

node {
    try {
        checkout scm

        parallel documentation: {
            stage('pdflatex & bibtex') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                dir (DOCUMENTATION_DIR) {
                    sh('./make.sh')
                }
            }
            stage('artifacts') {
                archiveArtifacts artifacts: "**/" + DOCUMENT_NAME + ".pdf", fingerprint: true
            }
        }, testClasses: {
        	stage('gradle testClasses') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    sh('./gradlew clean assemble testClasses')
                }
            }
        }, stopServices: {
            stage('stopping services') {
              sh('/var/lib/jenkins/deploy/stopAll.sh || true')
            }
        }


        parallel restTest: {
            stage('gradle attributeAuthority:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew attributeAuthority:test')
                    } finally {
                        step([$class: 'JUnitResultArchiver', testResults: 'attributeAuthority/build/test-results/test/*.xml'])
                    }
                }
            }
            stage('gralde bootjar') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // echo pwd()
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew attributeAuthority:bootjar')
                    } finally {
                        archiveArtifacts artifacts: "**/build/libs/*.jar", fingerprint: true
                    }
                }
            }

            stage('gradle centralServer:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew centralServer:test')
                    } finally {
                        step([$class: 'JUnitResultArchiver', testResults: 'centralServer/build/test-results/test/*.xml'])
                    }
                }
            }
            stage('gralde bootjar') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // echo pwd()
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew centralServer:bootjar')
                    } finally {
                        archiveArtifacts artifacts: "**/build/libs/*.jar", fingerprint: true
                    }
                }
            }

            stage('gradle cloudStorageProvider:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew cloudStorageProvider:test')
                    } finally {
                        step([$class: 'JUnitResultArchiver', testResults: 'cloudStorageProvider/build/test-results/test/*.xml'])
                    }
                }
            }
            stage('gralde bootjar') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // echo pwd()
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew cloudStorageProvider:bootjar')
                    } finally {
                        archiveArtifacts artifacts: "**/build/libs/*.jar", fingerprint: true
                    }
                }
            }
            
        },
        crypto: {
            stage('gradle crypto:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew crypto:test')
                    } finally {
                        step([$class: 'JUnitResultArchiver', testResults: 'crypto/build/test-results/test/*.xml'])
                    }
                }
            }
        },
        lib: {
            stage('gradle lib:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew lib:test')
                    } finally {
                        step([$class: 'JUnitResultArchiver', testResults: 'lib/build/test-results/test/*.xml'])
                    }
                }
            }
        },
        client: {
            stage('gradle client:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew client:test')
                    } finally {
                        step([$class: 'JUnitResultArchiver', testResults: 'client/build/test-results/test/*.xml'])
                    }
                }
            }
        }

        stage('integration:services') {
            copyArtifacts(
                projectName: "${env.JOB_NAME}", 
                selector: specific("${BUILD_NUMBER}"),
                target: '/var/lib/jenkins/deploy/',
                flatten: true, 
                filter: '**/*.jar');


            echo "Deploy artifacts."
            sh('/var/lib/jenkins/deploy/cb-integration-test.sh')
            echo "Dropped database"
            sh('/var/lib/jenkins/deploy/deploy.sh')
            echo "Redeployed"

            echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
            // sh('printenv')
            dir (SOURCE_DIR) {
                try {
                    sh('./gradlew integrationTest:test')
                } catch (e) {
                    archiveArtifacts artifacts: "/var/lib/jenkins/deploy/*.log", fingerprint: true
                    throw e
                } finally {
                    step([$class: 'JUnitResultArchiver', testResults: 'integrationTest/build/test-results/test/*.xml'])
                    archiveArtifacts artifacts: "**/test@tu-berlin.de.*", fingerprint: true
                }
            }
        }


        stage('integration:client') {
            dir (SOURCE_DIR) {
                try {
                    sh('./gradlew client:integrationTest')
                } catch (e) {
                    archiveArtifacts artifacts: "/var/lib/jenkins/deploy/*.log", fingerprint: true
                    throw e
                } finally {
                    step([$class: 'JUnitResultArchiver', testResults: 'client/build/test-results/test/*.xml'])
                    archiveArtifacts artifacts: "**/test@tu-berlin.de.*", fingerprint: true
                }
            }
        }
        currentBuild.result = 'SUCCESS'
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}
