@Library('my-library@master') _

import org.example.VMInfo
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
        stage('VM Info') {
            steps {
                script {
                    new VMInfo(this).run()
                }
            }
        }

        stage('Build and Package Java App') {
            steps {
                script {
                    // We run the build and package phase, skipping tests as they will be run in a separate stage.
                    new BuildJavaApp(this).run(
                        'true'
                    )
                }
            }
        }

        stage('Test Java App') {
            steps {
                script {
                    // This stage specifically runs the tests.
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
                    // Update this call to pass the Git URL and new version number
                    new DeployArgoCD(this).run(
                        'https://github.com/AhmedMagdy199/java.git', // Your Git repository URL
                        'k8s/deployment.yaml',                   // Path to your deployment manifest
                        'ahmedmadara/java-app',                   // The Docker image name
                        params.VERSION,                           // The new image version
                        'argocd-git'                              // The ID of your Git credentials in Jenkins
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
