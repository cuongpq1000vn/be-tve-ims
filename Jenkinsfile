pipeline {
    agent any

    environment {
        GITHUB_REPO = 'git@github.com:cuongpq1000vn/be-tve-ims.git'
        SSH_CREDENTIALS = 'github-ssh-key'
        ECR_REPO = '277707103845.dkr.ecr.ap-southeast-1.amazonaws.com/tri-viet-namespace/tri-viet'
        EC2_USER = 'ubuntu'
        EC2_HOST = 'ec2-user@18.141.181.255'
        AWS_REGION = 'ap-southeast-1'
        AWS_CREDENTIALS_ID = 'aws-credentials'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', credentialsId: SSH_CREDENTIALS, url: GITHUB_REPO
            }
        }

        stage('Check Docker & AWS CLI Availability') {
            steps {
                sh 'docker --version'
                sh 'aws --version'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                    cp /home/ec2-user/springboot-deploy/google-auth/client_secret.json ./client_secret.json
                    cp /home/ec2-user/springboot-deploy/google-auth/service_user.json ./service_user.json
                    docker build --build-arg SECRET_PATH=./client_secret.json --build-arg SERVICE_PATH=./service_user.json -t tve-ims:latest .
                    """
                }
            }
        }

        stage('Push Docker Image to ECR') {
            steps {
                script {
                    def tag = sh(script: "[[ \$(git tag | wc -l) -eq 0 ]] && echo 'latest' || git describe --tags --abbrev=0", returnStdout: true).trim()

                    withAWS(credentials: AWS_CREDENTIALS_ID, region: AWS_REGION) {
                        sh """
                        set -e
                        aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                        """
                    }

                    sh """
                    docker tag tve-ims:latest ${ECR_REPO}:${tag}
                    docker push ${ECR_REPO}:${tag}
                    """
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent([SSH_CREDENTIALS]) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ${EC2_HOST} << 'EOF'
                    set -e
                    docker pull ${ECR_REPO}:latest
                    cd ~/springboot-deploy
                    docker-compose down || true  # Nếu down lỗi thì vẫn tiếp tục
                    docker-compose up -d --remove-orphans
                    EOF
                    """
                }
            }
        }
    }
     post {
        success {
            echo 'Deployment completed successfully!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
