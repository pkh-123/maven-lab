pipeline {
    agent any

    stages {
        stage('CI - Maven build and test') {
            steps {
                sh 'sh ./mvnw -B clean package'
            }
        }

        stage('Check SSH access to VM') {
            steps {
                sh '''
                    ssh -i /var/jenkins_home/.ssh/deploy_key \
                        -p 2222 \
                        -o UserKnownHostsFile=/var/jenkins_home/.ssh/known_hosts \
                        -o StrictHostKeyChecking=accept-new \
                        adam@host.docker.internal 'hostname'
                '''
            }
        }
    }
}