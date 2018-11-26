#!/usr/bin/env groovy
buildPlugin(platforms: ['linux'], jdkVersions: [8], findbugs: [archive: true, unstableTotalAll: '0'], checkstyle: [run: true, archive: true])

//node {
//    stage 'post-build'
//    step([$class: 'MasterCoverageAction'])
//}