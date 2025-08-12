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

        stage('Build Java App') {
            steps {
                script {
                    new BuildJavaApp(this).run()
                }
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                script {
                    new BuildAndPushDocker(this).run(
                        'dockerhub',           // Your Jenkins Docker credentials ID
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
                        'https://192.168.56.11:31583',  // ArgoCD server URL (without trailing /app)
                        'java-app'                      // ArgoCD app name
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
