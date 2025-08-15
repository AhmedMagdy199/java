package org.example

class QualityGate implements Serializable {
    def script

    QualityGate(def script) { this.script = script }

    void run() {
        def qg = script.waitForQualityGate() // Jenkins SonarQube plugin
        if (qg.status != 'OK') {
            error "Pipeline failed due to quality gate: ${qg.status}"
        }
    }
}
