package org.example

class VMInfo implements Serializable {
    def script

    VMInfo(def script) {
        this.script = script
    }

    void run() {
        script.echo "Checking out source code..."
        script.checkout script.scm

        // Wrap the shell script with the tools block to set JAVA_HOME and PATH.
        script.tools {
            script.sh '''
                echo "Agent hostname: $(hostname)"
                echo "Java version:"
                java -version
                echo "Maven version:"
                mvn -version
            '''
        }
    }
}
