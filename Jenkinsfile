pipeline {
    agent {
        label 'general'
    }
    options {
        buildDiscarder logRotator(
            numToKeepStr:         (env.BRANCH_NAME == 'develop') ? '40' : '10',
            artifactNumToKeepStr: (env.BRANCH_NAME == 'develop') ? ''   : '1',
        )
        copyArtifactPermission(env.JOB_NAME)
        disableConcurrentBuilds abortPrevious: (env.BRANCH_NAME == 'develop') ? false : true
        quietPeriod( (env.BRANCH_NAME == 'develop') ? 0 : 7 )
        timeout activity: true, time: 60
        timestamps()
    }
    stages {
        stage('Build') {
            steps {
                sh label: 'Build', script: 'nvm install'
                archiveArtifacts artifacts: 'target/*.hpi'
            }
        }
    }
    post {
        always {
            script {
                slack.notify(notifyCommitters: true)
            }
        }
    }
}
