@Library('my-library@master') _

pipeline {
    agent { label 'java-app' }

    environment {
        // Project Configuration
        IMAGE_REPO      = '192.168.1.22:31564/my-repo'
        IMAGE_NAME      = 'java-web-app' 
        IMAGE_VERSION   = "${env.BUILD_NUMBER}"
        
        // SonarQube Configuration
        PROJECT_KEY     = 'java-web-app'
        PROJECT_NAME    = 'java-web-app' 
        SONAR_TOKEN     = 'sonarqube-token'
        SONAR_HOST_URL  = 'http://192.168.1.22:31000'
        
        // Other Credentials
        ARGO_CREDS      = 'argocdCred'
        SLACK_CREDS     = 'slack-token'
        GITHUB_URL      = 'https://github.com/AhmedMagdy199/java.git'
        K8S_YAML_PATH   = 'k8s/deployment.yaml'
        ARGOCD_SERVER   = '192.168.1.12:31360'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
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

        // DEBUG STAGE ADDED HERE
        stage('Verify SonarQube Config') {
            steps {
                script {
                    try {
                        def servers = Jenkins.instance.getDescriptor('hudson.plugins.sonar.SonarGlobalConfiguration').getInstallations()
                        echo "=== DEBUG: Configured SonarQube servers ==="
                        echo servers*.name.join("\n")
                        assert servers.any { it.name == 'sonar' }, 
                            "ERROR: No SonarQube server named 'sonar' found. Configure it in:\n" +
                            "Jenkins → Manage Jenkins → Configure System → SonarQube servers"
                    } catch (Exception e) {
                        error "DEBUG FAILED: ${e.message}\n" +
                              "Is the SonarQube plugin installed?"
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                container('maven') {
                    script {
                        withSonarQubeEnv('sonar') {
                            withCredentials([string(credentialsId: SONAR_TOKEN, variable: 'SONAR_AUTH_TOKEN']) {
                                sh """
                                    mvn sonar:sonar \
                                      -Dsonar.projectKey=${PROJECT_KEY} \
                                      -Dsonar.projectName=${PROJECT_NAME} \
                                      -Dsonar.host.url=${SONAR_HOST_URL} \
                                      -Dsonar.login=${SONAR_AUTH_TOKEN} \
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
                    timeout(time: 10, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
        }

        // ... rest of your stages (Docker build, Trivy scan, ArgoCD deploy) ...
    }

    // ... post section ...
}
