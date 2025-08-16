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
    }

    stages {

        stage('Checkout & Build Java') {
            steps {
                container('maven') {
                    git url: "${GITHUB_URL}"
                    script {
                        echo "=== Building Java Application ==="
                        new BuildJavaApp(this).run('false') 
                    }
                }
            }
        }

        //--------------------------------------------------------------------------------------------------

stage('SonarQube Analysis') {
    steps {
        container('maven') {  // Use existing maven container
            script {
                echo "=== Running SonarQube Analysis ==="
                withCredentials([string(credentialsId: SONAR_TOKEN, variable: 'TOKEN')]) {
                    sh """
                        # Install sonar-scanner if needed
                        if ! command -v sonar-scanner &> /dev/null; then
                            wget https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.7.0.2747-linux.zip
                            unzip sonar-scanner-cli-4.7.0.2747-linux.zip
                            export PATH=$PATH:$(pwd)/sonar-scanner-4.7.0.2747-linux/bin
                        fi
                        
                        sonar-scanner \
                          -Dsonar.projectKey=${PROJECT_KEY} \
                          -Dsonar.projectName="${PROJECT_NAME}" \
                          -Dsonar.sources=. \
                          -Dsonar.host.url=http://192.168.1.22:31000 \
                          -Dsonar.login=$TOKEN
                    """
                }
            }
        }
    }
}

        //--------------------------------------------------------------------------------------------------

     stage('Quality Gate') {
            steps {
                script {
                    echo "=== Checking SonarQube Quality Gate ==="
                    new QualityGate(this).run()
                }
            }
        }

        //--------------------------------------------------------------------------------------------------

        stage('Build & Push Docker Image') {
            steps {
                container('docker') {
                    script {
                        echo "=== Building and Pushing Docker Image ==="
                        def tag = "${IMAGE_REPO}/${IMAGE_NAME}:${IMAGE_VERSION}"
                        new BuildAndPushDocker(this).run('nexus-docker-cred', tag)
                    }
                }
            }
        }

        //--------------------------------------------------------------------------------------------------

        stage('OWASP Trivy Scan') {
            steps {
                container('docker') {
                    script {
                        echo "=== Scanning Docker Image for Vulnerabilities ==="
                        def tag = "${IMAGE_REPO}/${IMAGE_NAME}:${IMAGE_VERSION}"
                        sh "trivy image --exit-code 1 --severity HIGH,CRITICAL ${tag}"
                    }
                }
            }
        }

        //--------------------------------------------------------------------------------------------------

        stage('Deploy via ArgoCD') {
            when {
                expression { return currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    echo "=== Deploying to Kubernetes via ArgoCD ==="
                    new ArgoCDDeploy(this).run(
                        GITHUB_URL,
                        K8S_YAML_PATH,
                        "${IMAGE_REPO}/${IMAGE_NAME}",
                        IMAGE_VERSION,
                        ARGO_CREDS
                    )
                }
            }
        }
    }

    //--------------------------------------------------------------------------------------------------

    post {
        success {
            script {
                new SlackNotifier(this).notify(
                    "✅ Pipeline Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    SLACK_CREDS
                )
            }
        }
        failure {
            script {
                new SlackNotifier(this).notify(
                    "❌ Pipeline Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    SLACK_CREDS
                )
            }
        }
        always {
            cleanWs()
        }
    }
}
