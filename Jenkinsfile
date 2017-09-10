#!/usr/bin/env groovy
buildPlugin(platforms: ['linux'], jdkVersions: [7, 8], findbugs: [archive: true, unstableTotalAll: '0'], checkstyle: [run: true, archive: true])
step([$class: 'MasterCoverageAction'])