package org.example

class SonarQube implements Serializable {
    def script

    SonarQube(def script) { this.script = script }

    void run(String projectKey, String projectName, String sonarTokenCredentialId) {
        script.withCredentials([script.string(credentialsId: sonarTokenCredentialId, variable: 'SONAR_TOKEN')]) {
            script.sh """
                sonar-scanner \\
                -Dsonar.projectKey=${projectKey} \\
                -Dsonar.projectName="${projectName}" \\
                -Dsonar.sources=. \\
                -Dsonar.host.url=http://192.168.1.22:31000 \\
                -Dsonar.login=\$SONAR_TOKEN
            """
        }
    }
}
