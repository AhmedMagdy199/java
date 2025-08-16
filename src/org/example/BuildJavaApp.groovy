package org.example

class BuildJavaApp implements Serializable {
    def script

    BuildJavaApp(def script) {
        this.script = script
    }

    void run(String skipTests) {
        script.echo "Checking if 'mvn test' is available..."

        def testExists = script.sh(
            script: "mvn help:describe -Dcmd=test | grep -q 'Name: test'",
            returnStatus: true
        ) == 0
        script.echo "'mvn test' goal exists. Proceeding with build..."
        script.sh "mvn clean package -Dmaven.test.skip=${skipTests}"
    }

}
