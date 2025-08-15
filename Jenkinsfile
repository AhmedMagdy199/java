@Library('my-library@master') _

import org.example.BuildAndPushDocker
import org.example.DeployArgoCD
import org.example.NotifyOnFailure
import org.example.VMInfo

pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = 'dockerhub'   // DockerHub Jenkins credential ID
        SONARQUBE_CREDENTIALS = 'sonarqube-token' // SonarQube token ID in Jenkins
        SLACK_CREDENTIALS = 'slack-webhook-id'    // Slack Jenkins credential
    }

    parameters {
        string(name: 'VERSION', defaultValue: "${BUILD_NUMBER}", description: 'Docker image tag/version')
    }

    stages {
        stage('Prepare Environment') {
            steps {
                script {
                    // Setup Java/Maven inside the agent if needed
                    new VMInfo(this).run()
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                script {
                    new BuildAndPushDocker(this).run(
                        DOCKERHUB_CREDENTIALS,
                        'ahmedmadara/java-app',
                        params.VERSION
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh """
                        mvn clean verify sonar:sonar \
                        -Dsonar.projectKey=java-app \
                        -Dsonar.host.url=http://192.168.1.22:31000 \
                        -Dsonar.login=${SONARQUBE_CREDENTIALS}
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('OWASP / Image Scan') {
            steps {
                sh """
                    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image --severity HIGH,CRITICAL ahmedmadara/java-app:${params.VERSION}
                """
            }
        }

        stage('Deploy via ArgoCD') {
            steps {
                script {
                    new DeployArgoCD(this).run(
                        'https://github.com/AhmedMagdy199/java.git',
                        'k8s/deployment.yaml',
                        'ahmedmadara/java-app',
                        params.VERSION,
                        'argocdCred'
                    )
                }
            }
        }
    }

    post {
        success {
            slackSend(channel: '#devops', color: 'good', message: "Pipeline SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
        }
        failure {
            script {
                new NotifyOnFailure(this).run()
                slackSend(channel: '#devops', color: 'danger', message: "Pipeline FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
            }
        }
        always {
            cleanWs()
        }
    }
}
