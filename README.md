# Jenkins GitHub Coverage Updater

[![Build Status](https://travis-ci.org/terma/jenkins-github-coverage-updater.svg?branch=master)](https://travis-ci.org/terma/jenkins-github-coverage-updater)
[![Coverage Status](https://coveralls.io/repos/github/terma/jenkins-github-coverage-updater/badge.svg?branch=master)](https://coveralls.io/github/terma/jenkins-github-coverage-updater?branch=master)



Post code coverage status to GitHub pull request comments. Example:
![Example](https://raw.githubusercontent.com/terma/jenkins-github-coverage-updater/master/screenshot.png)

## Prerequisite

Plugin could be used only for Build triggered by [GitHub pull request builder plugin](https://wiki.jenkins-ci.org/display/JENKINS/GitHub+pull+request+builder+plugin)

Plugin use existent coverage reports. So ensure you have Cubertura or Jacoco setup. It could be done by Jenkins or Mave or Gradle etc. Plugin just uses reports.

## How to use

* Install plugin
 * Goto [releases](https://github.com/terma/jenkins-github-coverage-updater/releases)
 * Download latest version of plugin *.hpi* file
 * Install to your Jenkins [guide](https://wiki.jenkins-ci.org/display/JENKINS/Plugins)
* Configure plugin
 * GitHub API URL keep blank for GitHub.com and fill if for dedicated instance of GitHub, example: ```http(s)://hostname/api/v3/```
 * Set [Personal Access Token](https://github.com/blog/1509-personal-api-tokens) (or keep blank if anonymous access enabled)
* [Optional] Add *Record Master Coverage* post build step to build which test your master
* Add *Publish coverage to GitHub* post build step to build. It should be triggered by [GitHub pull request builder plugin](https://wiki.jenkins-ci.org/display/JENKINS/GitHub+pull+request+builder+plugin)
