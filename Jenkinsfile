@Library('my-library') _

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
                vmInfo()
            }
        }

        stage('Build Java App') {
            steps {
                buildJavaApp(params.TEST_SKIP)
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                buildAndPushDocker('bfe0c7aa-ba02-4e02-9f9f-0d4a071449cc', 'ahmedmadara/java-app', params.VERSION)
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            notifyOnFailure()
        }
    }
}
