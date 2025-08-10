package org.example

class VMInfo implements Serializable {
    def script

    VMInfo(def script) {
        this.script = script
    }

    void run() {
        script.echo "Checking out source code..."
        script.checkout script.scm

        script.sh '''
            echo "Agent hostname: $(hostname)"
            echo "Java version:"
            java -version
            echo "Maven version:"
            mvn -version
        '''
    }
}