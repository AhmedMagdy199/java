package org.example

class BuildAndPushDocker implements Serializable {
    def script

    BuildAndPushDocker(def script) {
        this.script = script
    }

    void run(String dockerCredId, String imageName, String version) {
        script.withCredentials([script.usernamePassword(
            credentialsId: dockerCredId, 
            usernameVariable: 'DOCKER_USER', 
            passwordVariable: 'DOCKER_PASSWORD'
        )]) {
            script.sh """
                echo "\$DOCKER_PASSWORD" | docker login -u "\$DOCKER_USER" --password-stdin
                docker build -t ${imageName}:${version} .
                docker push ${imageName}:${version}
            """
        }
    }
}