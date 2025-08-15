pipeline {
    agent any

    tools {
        jdk 'java-17'
        maven 'maven'
    }

    environment {
        JAVA_HOME = tool(name: 'java-17', type: 'jdk')
        M2_HOME   = tool(name: 'maven', type: 'maven')
        PATH      = "${tool('java-17')}/bin:${tool('maven')}/bin:${env.PATH}"
        SONAR_TOKEN = credentials('sonarqube-token')
    }

    parameters {
        string(name: 'VERSION', defaultValue: "${BUILD_NUMBER}", description: 'Docker image version')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Java App') {
            steps {
                sh '''
                    echo "JAVA_HOME=$JAVA_HOME"
                    echo "Maven location: $M2_HOME"
                    mvn clean package -Dmaven.test.skip=true
                '''
            }
        }

        stage('Test Java App') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh """
                        mvn sonar:sonar \
                        -Dsonar.projectKey=java-app \
                        -Dsonar.host.url=http://192.168.1.22:31000 \
                        -Dsonar.login=${SONAR_TOKEN}
                    """
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

        stage('Build and Push Docker') {
            steps {
                sh "docker build -t ahmedmadara/java-app:${params.VERSION} ."
                sh "docker push ahmedmadara/java-app:${params.VERSION}"
            }
        }

        stage('Deploy via ArgoCD') {
            steps {
                sh "argocd app sync java-app --grpc-web"
            }
        }
    }

    post {
        always { cleanWs() }
        failure { echo "Pipeline failed!" }
    }
}
