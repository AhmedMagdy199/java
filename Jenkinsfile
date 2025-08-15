@Library('my-library@master') _

import org.example.BuildAndPushDocker
import org.example.SonarQube
import org.example.QualityGate
import org.example.ArgoCDDeploy
import org.example.SlackNotifier

pipeline {
    agent any

    environment {
        IMAGE_NAME = 'ahmedmadara/java-app'
        IMAGE_VERSION = "${BUILD_NUMBER}"
    }

    stages {

        stage('Build & Push Docker Image') {
            steps {
                script {
                    new BuildAndPushDocker(this).run('docker-hub-cred-id', IMAGE_NAME, IMAGE_VERSION)
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    new SonarQube(this).run('java-app', 'Java App', 'sonar-token-id')
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

        stage('OWASP Trivy Scan') {
            steps {
                script {
                    sh """
                        trivy image --exit-code 1 --severity HIGH,CRITICAL ${IMAGE_NAME}:${IMAGE_VERSION}
                    """
                }
            }
        }

        stage('Deploy via ArgoCD') {
            steps {
                script {
                    new ArgoCDDeploy(this).run(
                        'https://github.com/AhmedMagdy199/java.git',
                        'k8s/deployment.yaml',
                        IMAGE_NAME,
                        IMAGE_VERSION,
                        'argocd-cred-id'
                    )
                }
            }
        }
    }

    post {
        success {
            script {
                new SlackNotifier(this).notify("Pipeline Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}", 'slack-token-id')
            }
        }
        failure {
            script {
                new SlackNotifier(this).notify("Pipeline Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}", 'slack-token-id')
            }
        }
        always {
            cleanWs()
        }
    }
}
