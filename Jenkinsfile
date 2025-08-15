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

    parameters {
        string(name: 'VERSION', defaultValue: "${BUILD_NUMBER}", description: 'Enter the version of the docker image')
    }

    stages {
        stage('VM Info and Tools Check') {
            steps {
                echo "Checking Java and Maven versions..."
                sh 'java -version'
                sh 'mvn -version'
            }
        }

        stage('Build Java App') {
            steps {
                script {
                    new BuildJavaApp(this).run(
                        'true'
                    )
                }
            }
        }

        stage('Test Java App') {
            steps {
                script {
                    new BuildJavaApp(this).run(
                        'false'
                    )
                }
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                script {
                    new BuildAndPushDocker(this).run(
                        'dockerhub',
                        'ahmedmadara/java-app',
                        params.VERSION
                    )
                }
            }
        }

        stage('Deploy to Kubernetes via ArgoCD') {
            steps {
                script {
                    new DeployArgoCD(this).run(
                        'https://github.com/AhmedMagdy199/java.git', // Your Git repository URL
                        'k8s/deployment.yaml',                   // Path to your deployment manifest
                        'ahmedmadara/java-app',                   // The Docker image name
                        params.VERSION,                           // The new image version
                        'argocdCred'                              // The ID of your Git credentials in Jenkins
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
