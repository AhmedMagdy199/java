package org.example

class DockerBuildPush implements Serializable {
    def script

    DockerBuildPush(def script) {
        this.script = script
    }

    /**
     * Build and push a container image using Podman inside a Kubernetes pod.
     *
     * @param dockerCredId - Jenkins credentials ID for container registry (username/password)
     * @param imageName    - Full image name including registry (e.g., docker.io/user/app)
     * @param version      - Image tag/version
     */
    void run(String dockerCredId, String imageName, String version) {
        script.withCredentials([script.usernamePassword(
            credentialsId: dockerCredId,
            usernameVariable: 'REG_USER',
            passwordVariable: 'REG_PASS'
        )]) {
            script.sh '''
                REGISTRY=$(echo ''' + imageName + ''' | awk -F/ '{print $1}')
                echo "$REG_PASS" | podman login -u "$REG_USER" --password-stdin $REGISTRY
                podman build -t ''' + imageName + ''':''' + version + ''' .
                podman push ''' + imageName + ''':''' + version + '''
            '''
        }
    }
}
