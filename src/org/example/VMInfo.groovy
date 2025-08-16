@Library('my-library@master') _

pipeline {
    agent { label 'java-app' }

    environment {
        IMAGE_REPO = '192.168.1.22:31565/my-repo'
        IMAGE_NAME = 'maven-sonar-cli'
        IMAGE_VERSION = "1.3"
    }

    stages {

        stage('Checkout & Build Java') {
            steps {
                container('maven') {
                    script {
                        // Using your BuildJavaApp class
                        new org.example.BuildJavaApp(this).run('false')
                    }
                }
            }
        }

        stage('Test') {
            steps {
                container('maven') {
                    sh 'mvn test'
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                container('docker') {
                    script {
                        new org.example.DockerBuildPush(this).run(
                            'nexus-docker-cred',
                            "${IMAGE_REPO}/${IMAGE_NAME}",
                            IMAGE_VERSION
                        )
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                container('maven') {
                    script {
                        new org.example.SonarQube(this).run(
                            'java-app',
                            'Java App',
                            'sonarqube-token'
                        )
                        new org.example.QualityGate(this).run()
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
                container('maven') {
                    script {
                        new org.example.ArgoCDDeploy(this).run(
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
    }

    post {
        success {
            script {
                new org.example.SlackNotifier(this).notify(
                    "Pipeline Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    'slack-token'
                )
            }
        }
        failure {
            script {
                new org.example.SlackNotifier(this).notify(
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

