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
                sh '''
                    echo "Agent hostname: $(hostname)"
                    echo "Java version:"
                    java -version
                    echo "Maven version:"
                    mvn -version
                '''
            }
        }

        stage('Build Java App') {
            steps {
                sh "mvn clean package -Dmaven.test.skip=${params.TEST_SKIP}"
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'bfe0c7aa-ba02-4e02-9f9f-0d4a071449cc', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh '''
                            echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USER" --password-stdin
                            docker build -t ahmedmadara/java-app:${VERSION} .
                            docker push ahmedmadara/java-app:${VERSION}
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
