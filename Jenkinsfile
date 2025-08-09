pipeline {
    agent any

    tools {
        jdk 'java-11'      // Your Jenkins JDK 11 tool name
        maven 'maven'      // Your Jenkins Maven tool name
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
                    withCredentials([usernamePassword(credentialsId: '38db19f1-894d-46ca-b9ef-e5309f649a32', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh """
                            echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USER} --password-stdin
                            docker build -t ahmedmadara/java-app:${params.VERSION} .
                            docker push ahmedmadara/java-app:${params.VERSION}
                        """
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
