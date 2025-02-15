pipeline {
    agent { label 'api-agent' }

    environment {
        GITHUB_REPO = 'git@github.com:cuongpq1000vn/be-tve-ims.git'
        SSH_CREDENTIALS = 'ec2-ssh-key'
        ECR_REPO = '277707103845.dkr.ecr.ap-southeast-1.amazonaws.com/tri-viet-namespace/tri-viet'
        EC2_USER = 'ubuntu'
        EC2_HOST = 'ec2-user@18.141.181.255'
        AWS_REGION = 'ap-southeast-1'
        AWS_CREDENTIALS_ID = 'aws-credentials'  // Ensure this is your AWS credentials in Jenkins
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
                    cp /home/quangnguyen16200/coffeeshop/cred/client_secret.json ./client_secret.json
                    cp /home/quangnguyen16200/coffeeshop/cred/service_user.json ./service_user.json
                    docker build --build-arg SECRET_PATH=./client_secret.json --build-arg SERVICE_PATH=./service_user.json -t longchass-be:latest .
                    """
                }
            }
        }

        stage('Push Docker Image to ECR') {
            steps {
                script {
                    def tag = sh(script: "git describe --tags --abbrev=0 || echo 'latest'", returnStdout: true).trim()

                    withAWS(credentials: AWS_CREDENTIALS_ID, region: AWS_REGION) {
                        sh """
                        aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                        """
                    }

                    sh """
                    docker tag longchass-be:latest ${ECR_REPO}:${tag}
                    docker push ${ECR_REPO}:${tag}
                    """
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent([SSH_CREDENTIALS]) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ${EC2_HOST} << EOF
                    docker pull ${ECR_REPO}:${tag}
                    cd ~/springboot-deploy
                    docker-compose down
                    docker-compose up -d
                    EOF
                    """
                }
            }
        }
    }
}
