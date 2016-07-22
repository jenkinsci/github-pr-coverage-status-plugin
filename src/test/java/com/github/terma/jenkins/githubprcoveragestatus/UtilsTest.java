/*

    Copyright 2015-2016 Artem Stasiuk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package com.github.terma.jenkins.githubprcoveragestatus;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void getUserRepo() {
        Assert.assertEquals(
                "terma/jenkins-github-coverage-updater",
                Utils.getUserRepo("https://github.com/terma/jenkins-github-coverage-updater"));

        Assert.assertEquals("terma/jenkins-github-coverage-updater",
                Utils.getUserRepo("https://github.com/terma/jenkins-github-coverage-updater.git"));

        Assert.assertEquals("terma/jenkins-github-coverage-updater",
                Utils.getUserRepo("git@github.com:terma/jenkins-github-coverage-updater.git"));

        Assert.assertEquals("terma/jenkins-github-coverage-updater",
                Utils.getUserRepo("git@github.com:terma/jenkins-github-coverage-updater"));
    }

    @Test
    public void getJenkinsUrlFromBuildUrl() {
        Assert.assertEquals(
                "http://localhost:8080/jenkins",
                Utils.getJenkinsUrlFromBuildUrl("http://localhost:8080/jenkins/job/branch/45"));

        Assert.assertEquals(
                "http://localhost:8080",
                Utils.getJenkinsUrlFromBuildUrl("http://localhost:8080/job/branch/459000"));
    }

}
