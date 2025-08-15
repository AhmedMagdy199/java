package org.example

class SlackNotifier implements Serializable {
    def script

    SlackNotifier(def script) { this.script = script }

    void notify(String message, String slackToken) {
        script.withCredentials([script.string(credentialsId: slackToken, variable: 'SLACK_TOKEN')]) {
            script.sh """
                curl -X POST -H 'Authorization: Bearer \$SLACK_TOKEN' -H 'Content-type: application/json' \
                --data '{"channel":"#general","text":"${message}"}' \
                https://slack.com/api/chat.postMessage
            """
        }
    }
}
