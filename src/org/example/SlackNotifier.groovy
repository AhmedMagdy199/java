package org.example

class SlackNotifier implements Serializable {
    def script

    SlackNotifier(def script) {
        this.script = script
    }

    void notify(String message, String slackCredsId) {
        script.withCredentials([script.string(credentialsId: slackCredsId, variable: 'SLACK_WEBHOOK_URL')]) {
            script.sh """
                curl -X POST -H 'Content-type: application/json' --data '{"text":"${message}"}' \$SLACK_WEBHOOK_URL
            """
        }
    }
}
