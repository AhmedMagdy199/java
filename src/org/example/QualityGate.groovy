package org.example

class QualityGate implements Serializable {
    def script

    QualityGate(def script) { this.script = script }

    void run() {
        def qg = script.waitForQualityGate() // Jenkins SonarQube plugin
        script.echo "Quality Gate status: ${qg.status}"
        // Do not abort pipeline
    }
}
