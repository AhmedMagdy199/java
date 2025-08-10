def call(String dockerCredId, String imageName, String version) {
    withCredentials([usernamePassword(credentialsId: dockerCredId, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
        sh """
            echo "\$DOCKER_PASSWORD" | docker login -u "\$DOCKER_USER" --password-stdin
            docker build -t ${imageName}:${version} .
            docker push ${imageName}:${version}
        """
    }
}