pipeline {
    agent {
        kubernetes {
            yaml """
                apiVersion: v1
                kind: Pod
                spec:
                    containers:
                    - name: maven
                      image: maven:3.9-eclipse-temurin-17-alpine
                      command:
                      - cat
                      tty: true
                      resources:
                        requests:
                          cpu: "250m"
                    - name: docker
                      image: docker:24.0.5-dind
                      command:
                      - cat
                      tty: true
                      securityContext:
                        privileged: true
                      resources:
                        requests:
                          cpu: "250m"
                    - name: dind-sidecar
                      image: docker:24.0.5-dind
                      securityContext:
                        privileged: true
                      args: ["dockerd-entrypoint.sh"]
                      resources:
                        requests:
                          cpu: "250m"
            """
        }
    }

    parameters {
        string(name: 'VERSION', defaultValue: "${BUILD_NUMBER}", description: 'Enter the version of the docker image')
        choice(name: 'TEST_SKIP', choices: ['true', 'false'], description: 'Skip tests')
    }

    stages {
        stage('VM Info') {
            steps {
                container('maven') {
                    sh '''
                        echo "Agent hostname: $(hostname)"
                        echo "Agent IP addresses: $(hostname -I)"
                        echo "Maven version: $(mvn -v)"
                        echo "Java version: $(java -version)"
                    '''
                }
            }
        }

        stage('Build Java App') {
            steps {
                container('maven') {
                    sh "mvn clean package -Dmaven.test.skip=${params.TEST_SKIP}"
                }
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: '38db19f1-894d-46ca-b9ef-e5309f649a32', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                        container('docker') {
                            sh "echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USER} --password-stdin"
                            def imageName = "ahmedmadara/java-app"  
                            def imageTag = "${imageName}:${params.VERSION}"
                            sh "docker build -t ${imageTag} ."
                            sh "docker push ${imageTag}"
                        }
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
