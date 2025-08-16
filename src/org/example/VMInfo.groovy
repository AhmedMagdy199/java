package org.example

class VMInfo implements Serializable {
    def script

    VMInfo(def script) {
        this.script = script
    }

    void run() {
        // Checkout source code
        script.echo "Checking out source code..."
        script.checkout script.scm

        // Retrieve tool paths
        def javaHome = script.tool name: 'java-17'
        def mavenHome = script.tool name: 'maven'

        // Set environment for Java and Maven
        script.withEnv([
            "JAVA_HOME=${javaHome}",
            "M2_HOME=${mavenHome}",
            "PATH=${javaHome}/bin:${mavenHome}/bin:${script.env.PATH}"
        ]) {
            script.sh '''
                echo "Agent hostname: $(hostname)"
                echo "Java version:"
                java -version
                echo "Maven version:"
                mvn -version
                echo "Building project..."
                mvn clean package -Dmaven.test.skip=true
            '''
        }
    }
}
