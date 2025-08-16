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
        // Centralized variables for easy management
        IMAGE_REPO      = '192.168.1.22:31564/my-repo'
        IMAGE_NAME      = 'java-web-app' 
        IMAGE_VERSION   = "${env.BUILD_NUMBER}"
        PROJECT_KEY     = 'java-web-app' // Must match the SonarQube project key
        PROJECT_NAME    = 'java-web-app' 
        SONAR_TOKEN     = 'sonarqube-token' // Jenkins credential ID for SonarQube token
        ARGO_CREDS      = 'argocdCred' // Jenkins credential ID for ArgoCD
        SLACK_CREDS     = 'slack-token' // Jenkins credential ID for Slack
        GITHUB_URL      = 'https://github.com/AhmedMagdy199/java.git'
        K8S_YAML_PATH   = 'k8s/deployment.yaml'
    }

    stages {

        stage('Checkout & Build Java') {
            steps {
                container('maven') {
                    // Checkout the source code from your public GitHub repository
                    git url: "${GITHUB_URL}"
                    script {
                        echo "=== Building Java Application ==="
                        // Assumes BuildJavaApp will run `mvn clean package`
                        new BuildJavaApp(this).run('false') 
                    }
                }
            }
        }
        
        //--------------------------------------------------------------------------------------------------

        stage('SonarQube Analysis') {
            steps {
                container('maven') {
                    script {
                        echo "=== Running SonarQube Analysis ==="
                        // Run analysis on the project and publish to SonarQube server
                        new SonarQube(this).run(
                            PROJECT_KEY,
                            PROJECT_NAME,
                            SONAR_TOKEN
                        )
                    }
                }
            }
        }
        
        //--------------------------------------------------------------------------------------------------

        stage('Quality Gate') {
            steps {
                script {
                    echo "=== Checking SonarQube Quality Gate ==="
                    // Poll SonarQube for the Quality Gate status
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
                        // Build Docker image from the application artifact and push to the registry
                        new BuildAndPushDocker(this).run(
                            'nexus-docker-cred',
                            tag
                        )
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
                        // Scan the pushed image and fail the pipeline on high/critical issues
                        sh "trivy image --exit-code 1 --severity HIGH,CRITICAL ${tag}"
                    }
                }
            }
        }
        
        //--------------------------------------------------------------------------------------------------

        stage('Deploy via ArgoCD') {
            when {
                // Ensure this stage only runs if the build and security checks were successful
                expression { return currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    echo "=== Deploying to Kubernetes via ArgoCD ==="
                    // Update the GitOps repository to trigger an ArgoCD sync
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
            // Clean up the Jenkins workspace after every build
            cleanWs()
        }
    }
}
