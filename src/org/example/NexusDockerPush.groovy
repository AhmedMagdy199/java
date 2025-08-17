package org.example

class NexusDockerPush implements Serializable {
    def script

    NexusDockerPush(def script) {
        this.script = script
    }

    /**
     * Build and push a container image to the Nexus registry.
     *
     * @param dockerCredId - Jenkins credentials ID for Nexus registry (username/password)
     * @param imageName    - Full image name including registry (e.g., 192.168.1.22:31564/app)
     * @param version      - Image tag/version
     */
    void run(String dockerCredId, String imageName, String version) {
        def nexusRegistry = "http://192.168.1.22:31564/"

        script.withCredentials([script.usernamePassword(
            credentialsId: dockerCredId,
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASSWORD'
        )]) {
            script.sh """
                echo "\$DOCKER_PASSWORD" | docker login -u "\$DOCKER_USER" --password-stdin ${nexusRegistry}
                docker build -t ${imageName}:${version} .
                docker push ${imageName}:${version}
            """
        }
    }
}