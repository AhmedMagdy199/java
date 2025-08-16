@Library('my-library@master') _

import org.example.BuildJavaApp
import org.example.SonarQube
import org.example.QualityGate
import org.example.BuildAndPushDocker
import org.example.ArgoCDDeploy
import org.example.SlackNotifier

pipeline {
    agent { label 'java-app' }

    environment {
        IMAGE_REPO    = '192.168.1.22:31564/my-repo'
        IMAGE_NAME    = 'maven-sonar-cli'
        IMAGE_VERSION = '1.4'
    }

    stages {

        stage('Checkout & Build Java') {
            steps {
                container('maven') { 
                    script {
                        new BuildJavaApp(this).run('false') // Run tests
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                container('maven') {
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

        stage('Build & Push Docker Image') {
            steps {
                container('docker') { // Ensure Docker socket is mounted
                    script {
                        new BuildAndPushDocker(this).run(
                            'nexus-docker-cred',
                            "${IMAGE_REPO}/${IMAGE_NAME}",
                            IMAGE_VERSION
                        )
                    }
                }
            }
        }

        stage('OWASP Trivy Scan') {
            steps {
                container('docker') {
                    sh """
                        trivy image --exit-code 1 --severity HIGH,CRITICAL ${IMAGE_REPO}/${IMAGE_NAME}:${IMAGE_VERSION}
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
                        "${IMAGE_REPO}/${IMAGE_NAME}",
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
                    'slack-token' // pass credential ID, not variable
                )
            }
        }
        failure {
            script {
                new SlackNotifier(this).notify(
                    "Pipeline Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    'slack-token'
                )
            }
        }
        always {
            cleanWs()
        }
    }
}
