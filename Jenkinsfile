@Library('my-library@master') _

pipeline {
    agent { label 'java-app' }

    environment {
        IMAGE_REPO      = '192.168.1.22:31565/my-repo'
        IMAGE_NAME      = 'java-web-app'  
        IMAGE_VERSION   = "${env.BUILD_NUMBER}"
        PROJECT_KEY     = 'java-web-app'
        PROJECT_NAME    = 'java-web-app'  
        SONAR_TOKEN     = 'sonarqube-token'
        ARGO_CREDS      = 'argocd-git'
        SLACK_CREDS     = 'slack-token1'
        GITHUB_URL      = 'https://github.com/AhmedMagdy199/java.git'
        K8S_YAML_PATH   = 'k8s/deployment.yaml'
        ARGOCD_SERVER   = '192.168.1.22:31983'
        SONAR_HOST_URL  = 'http://192.168.1.22:31000'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Setup Environment') {
            steps {
                container('maven') {
                    script {
                        sh '''
                            echo "Environment Setup Complete"
                            echo "Java: $(java -version 2>&1 | head -n 1)"
                            echo "Maven: $(mvn -version | head -n 1)"
                        '''
                    }
                }
            }
        }

        stage('Build') {
            steps {
                container('maven') {
                    script {
                        new org.example.BuildJavaApp(this).run('false')
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                container('maven') {
                    script {
                        withCredentials([string(credentialsId: SONAR_TOKEN, variable: 'SONAR_AUTH_TOKEN')]) {
                            withSonarQubeEnv('sonarqube') {
                                sh """
                                    mvn sonar:sonar \\
                                     -Dsonar.projectKey=${PROJECT_KEY} \\
                                     -Dsonar.projectName=${PROJECT_NAME} \\
                                     -Dsonar.host.url=${SONAR_HOST_URL} \\
                                     -Dsonar.login=${SONAR_AUTH_TOKEN} \\
                                     -Dsonar.java.binaries=target/classes
                                """
                            }
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    echo "SonarQube analysis submitted. Quality Gate status will be available in SonarQube UI."
                }
            }
        }

        stage('Build & Push Docker Image to Docker Hub') {
            steps {
                container('docker') {
                    script {
                        def hubImage = "docker.io/ahmedmadara/${IMAGE_NAME}"
                        new org.example.BuildAndPushDocker(this).run(
                            '5ba0c530-1d43-4d52-b28c-03b368f8fb73',
                            hubImage,
                            IMAGE_VERSION
                        )
                    }
                }
            }
        }

        stage('Deploy via ArgoCD') {
            when {
                expression { return currentBuild.result == 'SUCCESS' || currentBuild.result == 'UNSTABLE' }
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
