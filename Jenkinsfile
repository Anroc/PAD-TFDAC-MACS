// http://your.jenkins.host/scriptApproval/
import groovy.json.JsonOutput

DOCUMENT_NAME = "multi-authority-abe"
DOCUMENTATION_DIR = "./thesis"
SOURCE_DIR = "./implementation/tfdacmacs"

node {
    try {
        checkout scm

        stage('gradle crypto:test') {
            echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
            // sh('printenv')
            dir (SOURCE_DIR) {
                try {
                    sh('./gradlew crypto:clean crypto:test')
                } finally {
                    step([$class: 'JUnitResultArchiver', testResults: 'crypto/build/test-results/test/*.xml'])
                }
            }
        }

        stage('gradle lib:test') {
            echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
            // sh('printenv')
            dir (SOURCE_DIR) {
                try {
                    sh('./gradlew lib:clean lib:test')
                } finally {
                    step([$class: 'JUnitResultArchiver', testResults: 'lib/build/test-results/test/*.xml'])
                }
            }
        }

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
        attributeAuthority: {
            stage('gradle attributeAuthority:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew attributeAuthority:clean attributeAuthority:test')
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
            
        },
        centralServer: {
            stage('gradle centralServer:test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                // sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew centralServer:clean centralServer:test')
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
            
        }

        stage('deploy') {
            if("${env.BRANCH_NAME}" == "feature/integration-test") {
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
