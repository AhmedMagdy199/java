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
        string(name: 'ARGOCD_SERVER', defaultValue: 'argocd.example.com', description: 'ArgoCD server URL')
        string(name: 'ARGOCD_APP_NAME', defaultValue: 'my-app', description: 'ArgoCD application name')
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
                        'dockerhub',      // Replace with your actual Jenkins Docker credential ID
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
                        params.ARGOCD_SERVER,
                        null,           // User and password are fetched from Jenkins credentials inside DeployArgoCD
                        null,
                        params.ARGOCD_APP_NAME
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
