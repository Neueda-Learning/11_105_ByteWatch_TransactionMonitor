pipeline {

    agent any

    environment {
        GIT_URL = 'https://github.com/Neueda-Learning/11_105_ByteWatch_TransactionMonitor.git'
        BRANCH = 'main'
        COMPOSE_FILE = "${WORKSPACE}/docker-compose.yml"
        COMPOSE_CMD = 'docker-compose'
    }

    stages {

        stage('Checkout Source') {
            steps {
                git branch: "${BRANCH}", url: "${GIT_URL}"
            }
        }

        stage('Build & Test Backend') {
            steps {
                dir('backend') {
                    // clean verify actually runs RuleEngineServiceTest / AlertServiceTest
                    // and the controller integration tests — the pipeline fails here if
                    // any of them fail, before an image is ever built.
                    sh 'mvn -B clean verify'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Preflight Checks') {
            steps {
                sh '''
                    command -v "$COMPOSE_CMD" >/dev/null 2>&1
                    echo "Workspace: $WORKSPACE"
                    pwd
                    ls -la
                    test -f "$COMPOSE_FILE"
                '''
            }
        }

        stage('Stop Existing Containers') {
            steps {
                sh '$COMPOSE_CMD -f "$COMPOSE_FILE" down || true'
            }
        }

        stage('Build Docker Images') {
            steps {
                // Tests already passed in the previous stage, so the backend
                // Dockerfile's own build step skips re-running them.
                sh '$COMPOSE_CMD -f "$COMPOSE_FILE" build --no-cache'
            }
        }

        stage('Deploy') {
            steps {
                // DB_PASSWORD is injected from Jenkins Credentials at deploy time —
                // never written to this file or to build logs.
                withCredentials([string(credentialsId: 'bytewatch-db-password', variable: 'DB_PASSWORD')]) {
                    sh '$COMPOSE_CMD -f "$COMPOSE_FILE" up -d'
                }
            }
        }

        stage('Verify') {
            steps {
                sh '''
                    for i in $(seq 1 10); do
                        if curl -fs http://localhost:8080/actuator/health | grep -q '"status":"UP"'; then
                            echo "Backend is healthy."
                            exit 0
                        fi
                        echo "Waiting for backend to become healthy... ($i/10)"
                        sleep 5
                    done
                    echo "Backend did not become healthy in time."
                    $COMPOSE_CMD -f "$COMPOSE_FILE" logs backend
                    exit 1
                '''
                sh 'docker ps'
            }
        }
    }

    post {
        failure {
            echo 'Pipeline failed — leaving containers as-is for inspection. Run "docker-compose -f <path-to-compose-file> logs" to debug.'
        }
        always {
            sh 'docker image prune -f || true'
        }
    }
}
