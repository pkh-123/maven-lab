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

        stage('Deploy Docker container on VM') {
            steps {
                sh '''
                    ssh -i /var/jenkins_home/.ssh/deploy_key \
                        -p 2222 \
                        -o UserKnownHostsFile=/var/jenkins_home/.ssh/known_hosts \
                        -o StrictHostKeyChecking=accept-new \
                        adam@host.docker.internal \
                        "docker rm -f calculator-docker-cd >/dev/null 2>&1 || true; docker run --restart unless-stopped -d --name calculator-docker-cd -p 8081:8080 calculator-docker-lab:jenkins-${BUILD_NUMBER}"
                '''
            }
        }

        stage('Verify deployed app') {
            steps {
                sh '''
                    ssh -i /var/jenkins_home/.ssh/deploy_key \
                        -p 2222 \
                        -o UserKnownHostsFile=/var/jenkins_home/.ssh/known_hosts \
                        -o StrictHostKeyChecking=accept-new \
                        adam@host.docker.internal \
                        'for i in $(seq 1 20); do curl -fsS "http://127.0.0.1:8081/add?num1=10&num2=20" | grep -qx 30 && exit 0; sleep 3; done; exit 1'
                '''
            }
        }
    }
}