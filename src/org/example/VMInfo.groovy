package org.example

class VMInfo implements Serializable {
    def script

    VMInfo(def script) {
        this.script = script
    }

    void run() {
        script.echo "Checking out source code..."
        script.checkout script.scm

        // The 'tool' step retrieves the path of the configured tools.
        def javaHome = script.tool name: 'java-17'
        def mavenHome = script.tool name: 'maven'

        // 'withEnv' is a block that sets environment variables for the shell command.
        // We are explicitly setting JAVA_HOME, M2_HOME and updating the PATH
        // to ensure Java and Maven are correctly found.
        script.withEnv(["JAVA_HOME=${javaHome}", "M2_HOME=${mavenHome}", "PATH+MAVEN=${mavenHome}/bin", "PATH+JAVA=${javaHome}/bin"]) {
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
