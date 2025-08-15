package org.example

class DockerBuildPush implements Serializable {
    def script

    DockerBuildPush(def script) {
        this.script = script
    }

    /**
     * Build and push a container image using Podman.
     *
     * @param dockerCredId - Jenkins credentials ID for container registry (username/password)
     * @param imageName    - Full image name including registry (e.g., quay.io/user/app)
     * @param version      - Image tag/version
     */
    void run(String dockerCredId, String imageName, String version) {
        script.withCredentials([script.usernamePassword(
            credentialsId: dockerCredId,
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASSWORD'
        )]) {
            script.sh """
                echo "\$DOCKER_PASSWORD" | podman login -u "\$DOCKER_USER" --password-stdin
                podman build -t ${imageName}:${version} .
                podman push ${imageName}:${version}
            """
        }
    }
}
