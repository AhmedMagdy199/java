@Library('my-library@master') _

pipeline {
    agent { label 'java-app' }

    environment {
        IMAGE_REPO      = '192.168.1.22:31564/my-repo'
        IMAGE_NAME      = 'java-web-app' 
        IMAGE_VERSION   = "${env.BUILD_NUMBER}"
        PROJECT_KEY     = 'java-web-app'
        PROJECT_NAME    = 'java-web-app' 
        SONAR_TOKEN     = 'sonarqube-token'
        ARGO_CREDS      = 'argocdCred'
        SLACK_CREDS     = 'slack-token'
        GITHUB_URL      = 'https://github.com/AhmedMagdy199/java.git'
        K8S_YAML_PATH   = 'k8s/deployment.yaml'
        ARGOCD_SERVER   = '192.168.1.12:31360'
    }

    stages {
        stage('Checkout & Build') {
            steps {
                container('maven') {
                    script {
                        new org.example.VMInfo(this).run()
                        new org.example.BuildJavaApp(this).run('false')
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                container('maven') {
                    script {
                        withSonarQubeEnv('sonar') {
                            new org.example.SonarQube(this).run(
                                PROJECT_KEY,
                                PROJECT_NAME,
                                SONAR_TOKEN
                            )
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    new org.example.QualityGate(this).run()
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                container('docker') {
                    script {
                        def tag = "${IMAGE_REPO}/${IMAGE_NAME}:${IMAGE_VERSION}"
                        new org.example.BuildAndPushDocker(this).run(
                            'nexus-docker-cred',
                            "${IMAGE_REPO}/${IMAGE_NAME}",
                            IMAGE_VERSION
                        )
                    }
                }
            }
        }

        stage('Security Scan') {
            steps {
                container('docker') {
                    script {
                        def tag = "${IMAGE_REPO}/${IMAGE_NAME}:${IMAGE_VERSION}"
                        sh "trivy image --exit-code 1 --severity HIGH,CRITICAL ${tag}"
                    }
                }
            }
        }

        stage('Deploy via ArgoCD') {
            when {
                expression { return currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    new org.example.DeployArgoCD(this).run(
                        ARGOCD_SERVER,
                        'java-app'
                    )
                }
            }
        }
    }

    post {
        success {
            script {
                new org.example.SlackNotifier(this).notify(
                    "✅ Pipeline Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    SLACK_CREDS
                )
            }
        }
        failure {
            script {
                new org.example.SlackNotifier(this).notify(
                    "❌ Pipeline Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    SLACK_CREDS
                )
                new org.example.NotifyOnFailure(this).run()
            }
        }
        always {
            cleanWs()
        }
    }
}
