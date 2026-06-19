pipeline {
    agent any

    triggers {
        githubPush()
        pollSCM('H/2 * * * *')
    }

    environment {
        PROJECT_ID = 'prefab-lamp-498812-u8'
        REGION = 'us-central1'
        REPOSITORY = 'ems-repo'
        IMAGE_NAME = 'ems-backend'
        IMAGE = "${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${IMAGE_NAME}:latest"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '=== Checkout Started ==='
                git url: 'https://github.com/chandbasha304/employee-service-backend.git',
                    branch: 'main'
                echo '=== Checkout Completed ==='
            }
        }

        stage('Run Tests') {
            steps {
                echo '=== Maven Tests Started ==='
                dir('ems') {
                    sh '''
                        chmod +x mvnw
                        ./mvnw clean test
                    '''
                }
                echo '=== Maven Tests Completed ==='
            }
        }

        stage('Build Application') {
            steps {
                echo '=== Maven Build (JAR Generation) Started ==='
                dir('ems') {
                    sh '''
                        ./mvnw package -DskipTests
                    '''
                }
                echo '=== Maven Build Completed ==='
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '=== Docker Build Started ==='
                sh '''
                    docker build -t ${IMAGE} ./ems
                '''
                echo '=== Docker Build Completed ==='
            }
        }

        stage('Authenticate Artifact Registry') {
            steps {
                echo '=== Artifact Registry Authentication Started ==='
                sh '''
                    gcloud auth configure-docker us-central1-docker.pkg.dev --quiet
                '''
                echo '=== Artifact Registry Authentication Completed ==='
            }
        }

        stage('Push Image') {
            steps {
                echo '=== Push Started ==='
                sh '''
                    docker push ${IMAGE}
                '''
                echo '=== Push Completed ==='
            }
        }

        stage('Deploy') {
            steps {
                echo '=== Deployment Started ==='
                sh '''
                    # Ensure the network exists
                    docker network create ems-network || true

                    docker pull ${IMAGE}

                    docker stop ems-backend || true
                    docker rm ems-backend || true

                    # Run with --network ems-network
                    docker run -d \
                        --name ems-backend \
                        --network ems-network \
                        -p 8080:8080 \
                        ${IMAGE}

                    docker ps
                '''
                echo '=== Deployment Completed ==='
            }
        }
    }

    post {
        success {
            echo '====================================='
            echo 'PIPELINE EXECUTED SUCCESSFULLY'
            echo '====================================='
        }
        failure {
            echo '====================================='
            echo 'PIPELINE FAILED'
            echo '====================================='
        }
        always {
            echo '====================================='
            echo 'PIPELINE FINISHED'
            echo '====================================='
        }
    }
}
