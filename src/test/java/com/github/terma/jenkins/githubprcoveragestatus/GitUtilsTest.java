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


public class GitUtilsTest {

    @Test
    public void getUserRepo() {
        Assert.assertEquals(
                "terma/jenkins-github-coverage-updater",
                GitUtils.getUserRepo("https://github.com/terma/jenkins-github-coverage-updater"));

        Assert.assertEquals("terma/jenkins-github-coverage-updater",
                GitUtils.getUserRepo("https://github.com/terma/jenkins-github-coverage-updater.git"));

        Assert.assertEquals("terma/jenkins-github-coverage-updater",
                GitUtils.getUserRepo("git@github.com:terma/jenkins-github-coverage-updater.git"));

        Assert.assertEquals("terma/jenkins-github-coverage-updater",
                GitUtils.getUserRepo("git@github.com:terma/jenkins-github-coverage-updater"));
    }

    @Test
    public void getRepoUrl() {
        try {
           GitUtils.getRepoUrl(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // all good
        }

        Assert.assertEquals(
                "https://github.com/terma/jenkins-github-coverage-updater",
                GitUtils.getRepoUrl("https://github.com/terma/jenkins-github-coverage-updater"));

        Assert.assertEquals("https://github.com/terma/jenkins-github-coverage-updater",
                GitUtils.getRepoUrl("https://github.com/terma/jenkins-github-coverage-updater.git"));

        Assert.assertEquals("git@github.com:terma/jenkins-github-coverage-updater",
                GitUtils.getRepoUrl("git@github.com:terma/jenkins-github-coverage-updater.git"));

        Assert.assertEquals("git@github.com:terma/jenkins-github-coverage-updater",
                GitUtils.getRepoUrl("git@github.com:terma/jenkins-github-coverage-updater"));

        Assert.assertEquals("https://github.com/terma/test",
                GitUtils.getRepoUrl("https://github.com/terma/test/pull/1"));

        Assert.assertEquals("http://github.com/terma/test",
                GitUtils.getRepoUrl("http://github.com/terma/test/pull/1"));

        Assert.assertEquals("https://github.com/terma/test",
                GitUtils.getRepoUrl("https://github.com/terma/test/tree/branch"));
    }

    @Test
    public void getRepoName() {
        Assert.assertEquals(
                "jenkins-github-coverage-updater",
                GitUtils.getRepoName("https://github.com/terma/jenkins-github-coverage-updater"));

        Assert.assertEquals("jenkins-github-coverage-updater",
                GitUtils.getRepoName("https://github.com/terma/jenkins-github-coverage-updater.git"));

        Assert.assertEquals("jenkins-github-coverage-updater",
                GitUtils.getRepoName("git@github.com:terma/jenkins-github-coverage-updater.git"));

        Assert.assertEquals("jenkins-github-coverage-updater",
                GitUtils.getRepoName("git@github.com:terma/jenkins-github-coverage-updater"));
    }

}
