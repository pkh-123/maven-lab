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

        stage('Send Docker build files to VM') {
            steps {
                sh '''
                    tar -cf - Dockerfile target/calculator-0.0.1-SNAPSHOT.war | \
                    ssh -i /var/jenkins_home/.ssh/deploy_key \
                        -p 2222 \
                        -o UserKnownHostsFile=/var/jenkins_home/.ssh/known_hosts \
                        -o StrictHostKeyChecking=accept-new \
                        adam@host.docker.internal \
                        'mkdir -p /home/adam/calculator-docker-build && tar -xf - -C /home/adam/calculator-docker-build'
                '''
            }
        }

        stage('Build Docker image on VM') {
            steps {
                sh '''
                    ssh -i /var/jenkins_home/.ssh/deploy_key \
                        -p 2222 \
                        -o UserKnownHostsFile=/var/jenkins_home/.ssh/known_hosts \
                        -o StrictHostKeyChecking=accept-new \
                        adam@host.docker.internal \
                        "docker build -t calculator-docker-lab:jenkins-${BUILD_NUMBER} /home/adam/calculator-docker-build"
                '''
            }
        }
    }
}