@Library('my-library@master') _

import org.example.BuildJavaApp
import org.example.BuildAndPushDocker
import org.example.DeployArgoCD
import org.example.NotifyOnFailure

pipeline {
    agent any

    tools {
        jdk 'java-17'
        maven 'maven'
    }

    environment {
        SONARQUBE_SERVER = 'sonarqube'              // Must match your Jenkins SonarQube configuration name
        SONAR_TOKEN = credentials('sonarqube-token') // Jenkins secret text credential containing SonarQube token
    }

    parameters {
        string(name: 'VERSION', defaultValue: "${BUILD_NUMBER}", description: 'Enter the version of the docker image')
    }

    stages {

        stage('Checkout Source') {
            steps {
                checkout scm
            }
        }

        stage('Build Java App') {
            steps {
                script {
                    new BuildJavaApp(this).run('true') // skip tests
                }
            }
        }

        stage('Test Java App') {
            steps {
                script {
                    new BuildJavaApp(this).run('false') // run tests
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    withSonarQubeEnv("${SONARQUBE_SERVER}") {
                        sh """
                            mvn clean verify sonar:sonar \
                            -Dsonar.projectKey=java-app \
                            -Dsonar.host.url=http://192.168.1.22:31000 \
                            -Dsonar.login=${SONAR_TOKEN}
                        """
                    }
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

        stage('Build and Push Docker Image') {
            steps {
                script {
                    new BuildAndPushDocker(this).run(
                        'dockerhub',                   // Docker registry type
                        'ahmedmadara/java-app',        // Docker image name
                        params.VERSION                 // Docker image tag
                    )
                }
            }
        }

        stage('Deploy to Kubernetes via ArgoCD') {
            steps {
                script {
                    new DeployArgoCD(this).run(
                        'https://github.com/AhmedMagdy199/java.git', // Git repo
                        'k8s/deployment.yaml',                        // Kubernetes manifest path
                        'ahmedmadara/java-app',                        // Docker image name
                        params.VERSION,                                // Docker image version
                        'argocdCred'                                  // Jenkins credentials ID for Git if required
                    )
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            script {
                new NotifyOnFailure(this).run()
            }
        }
    }
}
