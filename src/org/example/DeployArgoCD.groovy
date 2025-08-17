package org.example

class DeployArgoCD implements Serializable {
    def script

    DeployArgoCD(def script) {
        this.script = script
    }

    void run(String argocdServer, String appName) {
        script.withCredentials([script.usernamePassword(
            credentialsId: 'argocd-git',
            usernameVariable: 'ARGOCD_USER',
            passwordVariable: 'ARGOCD_PASS'
        )]) {
            script.sh """
                argocd login ${argocdServer} --username \$ARGOCD_USER --password \$ARGOCD_PASS --insecure
                argocd app sync ${appName} --prune --timeout 300
                argocd logout ${argocdServer}
            """
        }
    }
}
