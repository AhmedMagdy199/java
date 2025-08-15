package org.example

class ArgoCDDeploy implements Serializable {
    def script

    ArgoCDDeploy(def script) { this.script = script }

    void run(String repoUrl, String manifestPath, String image, String version, String gitCredId) {
        script.sh """
            argocd login 192.168.1.12:31360 --username admin --password admin --insecure
            argocd app set java-app --repo ${repoUrl} --path ${manifestPath} --kube-context default --dest-server https://kubernetes.default.svc --dest-namespace default
            argocd app set java-app --image ${image}:${version}
            argocd app sync java-app
        """
    }
}
