@Library('my-library@master') _

import org.example.BuildAndPushDocker
import org.example.SonarQube
import org.example.QualityGate
import org.example.ArgoCDDeploy
import org.example.SlackNotifier

pipeline {
    agent { label 'java-app' }

    environment {
        // Use your Nexus repository address as the base for all image names
        IMAGE_REPO = '192.168.1.22:31565/my-repo'
        IMAGE_NAME = 'maven-sonar-cli'
        IMAGE_VERSION = "${BUILD_NUMBER}"
    }

    stages {
        stage('Build with Maven & SonarQube Analysis') {
            steps {
                container('maven') { // This container uses your custom image
                    sh 'mvn clean package -DskipTests'
                    script {
                        new SonarQube(this).run('java-app', 'Java App', 'sonarqube-token')
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    new QualityGate(this).run()
                }
            }
        }

        stage('Build & Push Final Docker Image') {
            steps {
                script {
                    // Use the nexus-docker-cred for authentication
                    new BuildAndPushDocker(this).run('nexus-docker-cred', "${IMAGE_REPO}/${IMAGE_NAME}", IMAGE_VERSION)
                }
            }
        }

        stage('OWASP Trivy Scan') {
            steps {
                // Use the correct image path from your Nexus registry
                sh """
                    trivy image --exit-code 1 --severity HIGH,CRITICAL ${IMAGE_REPO}/${IMAGE_NAME}:${IMAGE_VERSION}
                """
            }
        }

        stage('Deploy via ArgoCD') {
            steps {
                script {
                    new ArgoCDDeploy(this).run(
                        'https://github.com/AhmedMagdy199/java.git',
                        'k8s/deployment.yaml',
                        "${IMAGE_REPO}/${IMAGE_NAME}", // Use the new image name from Nexus
                        IMAGE_VERSION,
                        'argocdCred'
                    )
                }
            }
        }
    }

    post {
        success {
            script {
                withCredentials([string(credentialsId: 'slack-token', variable: 'slackToken')]) {
                    new SlackNotifier(this).notify(
                        "Pipeline Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        "${slackToken}"
                    )
                }
            }
        }
        failure {
            script {
                withCredentials([string(credentialsId: 'slack-token', variable: 'slackToken')]) {
                    new SlackNotifier(this).notify(
                        "Pipeline Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        "${slackToken}"
                    )
                }
            }
        }
        always {
            cleanWs()
        }
    }
}
