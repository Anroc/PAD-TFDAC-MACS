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
            stage('gradle test') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                sh('printenv')
                dir (SOURCE_DIR) {
                    try {
                        // sh('./gradlew clean test')
                    } finally {
                        //step([$class: 'JUnitResultArchiver', testResults: '**/test-results/test/*.xml'])
                    }
                }
            }
            stage('gralde bootjar') {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                echo pwd()
                dir (SOURCE_DIR) {
                    try {
                        sh('./gradlew bootjar')
                    } finally {
                        archiveArtifacts artifacts: "**/build/libs/*.jar", fingerprint: true
                    }
                }
            }
            
        }

        stage('deploy') {
            if("${env.BRANCH_NAME}" == "automatic-deply") {
                copyArtifacts(
                    projectName: "${env.JOB_NAME}", 
                    selector: "${BUILD_NUMBER}",
                    target: '/var/lib/jenkins/deploy/',
                    flatten: true, 
                    filter: '**/*.jar');


                echo "Deploy artifacts."
                sh('/var/lib/jenkins/deploy/deploy.sh')
            }
        }
        currentBuild.result = 'SUCCESS'
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}
