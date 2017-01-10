// Jenkinsfile (Pipeline Script)
node {
    // Configure environment
    env.JAVA_HOME = tool name: 'JDK8'
    env.REPO_URL = "repo.koc.se/hal.git" //scm.getUserRemoteConfigs()[0].getUrl()
    env.BUILD_NAME = "BUILD-" + env.BUILD_ID


    checkout scm

    stage('Build') {
        sh 'ant clean'
        sh 'ant build'
    }

    stage('Test') {
        try {
            sh 'ant test'
        } finally {
            step([$class: 'JUnitResultArchiver', testResults: 'build/reports/*.xml'])
        }
    }


    stage('Package') {
        sh 'ant package'
        archiveArtifacts artifacts: 'build/release/*', fingerprint: true

        // Tag artifact
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'f8e5f6c6-4adb-4ab2-bb5d-1c8535dff491',
                                      usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
            sh "git tag ${env.BUILD_NAME}"
            sh "git push 'https://${USERNAME}:${PASSWORD}@${env.REPO_URL}' ${env.BUILD_NAME}"
        }
    }
}

//stage('Deploy') {
//    input message: 'Deploy?', submitter: 'ziver'
//    node {
//        sh 'ant deploy'
//    }
//}
