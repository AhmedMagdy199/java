def call() {
    sh '''
        echo "Agent hostname: $(hostname)"
        echo "Java version:"
        java -version
        echo "Maven version:"
        mvn -version
    '''
}