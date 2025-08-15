@Library('my-library@master') _

import org.example.BuildAndPushDocker
import org.example.SonarQube
import org.example.QualityGate
import org.example.ArgoCDDeploy
import org.example.SlackNotifier

pipeline {
    agent { label 'java-app' }

    environment {
        IMAGE_NAME = 'ahmedmadara/java-app'
        IMAGE_VERSION = "${BUILD_NUMBER}"
        SONAR_TOKEN = 'sonarqube-token'
    }

    stages {
        stage('Build with Maven') {
            steps {
                container('maven') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                container('docker') {
                    script {
                        new BuildAndPushDocker(this).run('5ba0c530-1d43-4d52-b28c-03b368f8fb73', IMAGE_NAME, IMAGE_VERSION)
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    new SonarQube(this).run('java-app', 'Java App', SONAR_TOKEN)
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
                container('docker') {
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
                        'argocdCred'
                    )
                }
            }
        }
    }

    post {
        success {
            script {
                new SlackNotifier(this).notify(
                    "Pipeline Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    credentials('slack-token')
                )
            }
        }
        failure {
            script {
                new SlackNotifier(this).notify(
                    "Pipeline Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    credentials('slack-token-id')
                )
            }
        }
        always {
            cleanWs()
        }
    }
}
