@Library('my-library@master') _

import org.example.VMInfo
import org.example.BuildJavaApp
import org.example.BuildAndPushDocker
import org.example.NotifyOnFailure

pipeline {
    agent any

    tools {
        jdk 'java-17'
        maven 'maven'
    }

    parameters {
        string(name: 'VERSION', defaultValue: "${BUILD_NUMBER}", description: 'Enter the version of the docker image')
        choice(name: 'TEST_SKIP', choices: ['true', 'false'], description: 'Skip tests')
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
                    new BuildJavaApp(this).run(params.TEST_SKIP)
                }
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                script {
                    new BuildAndPushDocker(this).run(
                        'bfe0c7aa-ba02-4e02-9f9f-0d4a071449cc',
                        'ahmedmadara/java-app',
                        params.VERSION
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
