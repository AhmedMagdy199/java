def call(String skipTests) {
    echo "Checking if 'mvn test' is available in this project..."

    // This checks if there's a test phase in Maven's lifecycle for this project
    def testExists = sh(script: "mvn help:describe -Dcmd=test | grep -q 'Name: test'", returnStatus: true) == 0

    if (!testExists) {
        error(" Maven 'test' goal is not available in this project. Aborting pipeline.")
    }

    echo " 'mvn test' goal exists. Proceeding with build..."
    sh "mvn clean package -Dmaven.test.skip=${skipTests}"
}