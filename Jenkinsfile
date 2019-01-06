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
        },
        java: {
            stage('gradle attributeAuthority:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew attributeAuthority:test')
                    } finally {
                        step([$class: 'JUnitResultArchiver', testResults: '**/test-results/test/*.xml'])
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
            
        },
        java: {
            stage('gradle centralServer:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew centralServer:test')
                    } finally {
                        step([$class: 'JUnitResultArchiver', testResults: '**/test-results/test/*.xml'])
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
            
        },
        java: {
            stage('gradle crypto:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew crypto:test')
                    } finally {
                        step([$class: 'JUnitResultArchiver', testResults: '**/test-results/test/*.xml'])
                    }
                }
            }
        },
        java: {
            stage('gradle lib:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew lib:test')
                    } finally {
                        step([$class: 'JUnitResultArchiver', testResults: '**/test-results/test/*.xml'])
                    }
                }
            }
        }

        stage('deploy') {
            if("${env.BRANCH_NAME}" == "master") {
                copyArtifacts(
                    projectName: "${env.JOB_NAME}", 
                    selector: specific("${BUILD_NUMBER}"),
                    target: '/var/lib/jenkins/deploy/',
                    flatten: true, 
                    filter: '**/*.jar');


                // echo "Deploy artifacts."
                // sh('/var/lib/jenkins/deploy/deploy.sh')
            }
        }
        currentBuild.result = 'SUCCESS'
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}
