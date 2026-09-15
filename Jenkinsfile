pipeline {
    agent any

    stages {
        stage('CI - Maven build and test') {
            steps {
                sh 'sh ./mvnw -B clean package'
            }
        }
    }
}