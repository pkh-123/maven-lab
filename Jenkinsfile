pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    stages {
        stage('CI - Maven build and test') {
            steps {
                sh 'sh ./mvnw -B clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube-local') {
                    sh '''
                        sh ./mvnw -B \
                            org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                            -Dsonar.projectKey=maven-lab \
                            -Dsonar.projectName=maven-lab
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Check SSH access to VM') {
            steps {
                retry(3) {
                    sh '''
                        ssh -i /var/jenkins_home/.ssh/deploy_key \
                            -p 2222 \
                            -o ConnectTimeout=10 \
                            -o ServerAliveInterval=5 \
                            -o ServerAliveCountMax=2 \
                            -o UserKnownHostsFile=/var/jenkins_home/.ssh/known_hosts \
                            -o StrictHostKeyChecking=accept-new \
                            adam@host.docker.internal 'hostname'
                    '''
                }
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
                        "docker build \
                            -t calculator-docker-lab:jenkins-${BUILD_NUMBER} \
                            /home/adam/calculator-docker-build"
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
                        "docker rm -f calculator-docker-cd >/dev/null 2>&1 || true; \
                         docker run \
                            --restart unless-stopped \
                            -d \
                            --name calculator-docker-cd \
                            -p 8081:8080 \
                            calculator-docker-lab:jenkins-${BUILD_NUMBER}"
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
                        'for i in $(seq 1 40); do
                            curl -fsS "http://127.0.0.1:8081/add?num1=10&num2=20" \
                                | grep -qx 30 && exit 0
                            sleep 3
                        done
                        exit 1'
                '''
            }
        }

        stage('Publish image to Docker Hub') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'dockerhub-token',
                        variable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    sh '''
                        set +x

                        printf '%s' "$DOCKERHUB_TOKEN" | \
                        ssh -i /var/jenkins_home/.ssh/deploy_key \
                            -p 2222 \
                            -o UserKnownHostsFile=/var/jenkins_home/.ssh/known_hosts \
                            -o StrictHostKeyChecking=accept-new \
                            adam@host.docker.internal \
                            "set -e; \
                             trap 'docker logout >/dev/null 2>&1 || true' EXIT; \
                             docker login -u pp0104 --password-stdin; \
                             docker tag \
                                calculator-docker-lab:jenkins-${BUILD_NUMBER} \
                                pp0104/calculator-docker-lab:jenkins-${BUILD_NUMBER}; \
                             docker push \
                                pp0104/calculator-docker-lab:jenkins-${BUILD_NUMBER}; \
                             docker tag \
                                calculator-docker-lab:jenkins-${BUILD_NUMBER} \
                                pp0104/calculator-docker-lab:latest; \
                             docker push \
                                pp0104/calculator-docker-lab:latest"
                    '''
                }
            }
        }

        stage('Update GitOps image tag') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'github-gitops',
                        usernameVariable: 'GITHUB_USERNAME',
                        passwordVariable: 'GITHUB_TOKEN'
                    )
                ]) {
                    sh '''
                        set -eu

                        rm -rf calculator-gitops

                        git clone \
                            https://github.com/pkh-123/calculator-gitops.git \
                            calculator-gitops

                        cd calculator-gitops

                        grep -q 'newTag:' manifests/kustomization.yaml

                        sed -i \
                            "s/newTag: .*/newTag: jenkins-${BUILD_NUMBER}/" \
                            manifests/kustomization.yaml

                        git config user.name "Jenkins"
                        git config user.email "jenkins@local"

                        git add manifests/kustomization.yaml

                        if git diff --cached --quiet; then
                            echo "GitOps image tag is already up to date"
                            exit 0
                        fi

                        git commit \
                            -m "Deploy calculator image jenkins-${BUILD_NUMBER}"

                        set +x

                        git push \
                            "https://${GITHUB_USERNAME}:${GITHUB_TOKEN}@github.com/pkh-123/calculator-gitops.git" \
                            HEAD:main
                    '''
                }
            }
        }
    }
}