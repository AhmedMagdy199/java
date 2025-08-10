package org.example

class NotifyOnFailure implements Serializable {
    def script

    NotifyOnFailure(def script) {
        this.script = script
    }

    void run() {
        script.echo 'Pipeline failed!'
    }
}