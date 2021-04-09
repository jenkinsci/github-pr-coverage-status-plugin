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

public class MessageTest {

    @Test
    public void buildNiceForConsole() {
        Assert.assertEquals("Coverage 100% changed 0.0% vs master 100%", new Message(1, 1).forConsole("master"));
        Assert.assertEquals("Coverage 0% changed 0.0% vs master 0%", new Message(0, 0).forConsole("master"));
        Assert.assertEquals("Coverage 50% changed +50.0% vs master 0%", new Message(0.5f, 0).forConsole("master"));
        Assert.assertEquals("Coverage 0% changed -50.0% vs master 50%", new Message(0, 0.5f).forConsole("master"));
        Assert.assertEquals("Coverage 70% changed +20.0% vs master 50%", new Message(0.7f, 0.5f).forConsole("master"));
        Assert.assertEquals("Coverage 0% changed +0.02% vs staging 0%", new Message(0.0007f, 0.0005f).forConsole("staging"));
        Assert.assertEquals("Coverage 0% changed 0.0% vs develop 0%", new Message(0.000007f, 0.000005f).forConsole("develop"));
    }

    @Test
    public void buildNiceForIcon() {
        Assert.assertEquals("100% (0.0%) vs master 100%", new Message(1, 1).forIcon("master"));
        Assert.assertEquals("0% (0.0%) vs master 0%", new Message(0, 0).forIcon("master"));
        Assert.assertEquals("50% (+50.0%) vs master 0%", new Message(0.5f, 0).forIcon("master"));
        Assert.assertEquals("0% (-50.0%) vs master 50%", new Message(0, 0.5f).forIcon("master"));
        Assert.assertEquals("70% (+20.0%) vs master 50%", new Message(0.7f, 0.5f).forIcon("master"));
        Assert.assertEquals("69% (-0.7%) vs master 69%", new Message(0.686f, 0.693f).forIcon("master"));
        Assert.assertEquals("60% (+0.06%) vs master 60%", new Message(0.6007f, 0.6001f).forIcon("master"));
        Assert.assertEquals("0% (+0.01%) vs staging 0%", new Message(0.00007f, 0.00001f).forIcon("staging"));
        Assert.assertEquals("0% (0.0%) vs develop 0%", new Message(0.000007f, 0.000001f).forIcon("develop"));
    }

    @Test
    public void forCommentWithShieldIo() {
        String buildUrl = "http://terma.com/jenkins/job/ama";
        Assert.assertEquals(
                "[![100% (0.0%) vs master 100%](https://img.shields.io/badge/coverage-100%25%20(0.0%25)%20vs%20master%20100%25-brightgreen.svg)](http://terma.com/jenkins/job/ama)",
                new Message(1, 1).forComment(buildUrl, null, 80, 90, true, "master"));

        Assert.assertEquals(
                "[![0% (0.0%) vs master 0%](https://img.shields.io/badge/coverage-0%25%20(0.0%25)%20vs%20master%200%25-red.svg)](http://terma.com/jenkins/job/ama)",
                new Message(0, 0).forComment(buildUrl, null, 80, 90, true, "master"));

        Assert.assertEquals(
                "[![50% (+50.0%) vs master 0%](https://img.shields.io/badge/coverage-50%25%20(%2B50.0%25)%20vs%20master%200%25-red.svg)](http://terma.com/jenkins/job/ama)",
                new Message(0.5f, 0).forComment(buildUrl, null, 80, 90, true, "master"));

        Assert.assertEquals(
                "[![0% (-50.0%) vs staging 50%](https://img.shields.io/badge/coverage-0%25%20(--50.0%25)%20vs%20staging%2050%25-red.svg)](http://terma.com/jenkins/job/ama)",
                new Message(0, 0.5f).forComment(buildUrl, null, 80, 90, true, "staging"));

        Assert.assertEquals(
                "[![85% (+35.0%) vs develop 50%](https://img.shields.io/badge/coverage-85%25%20(%2B35.0%25)%20vs%20develop%2050%25-yellow.svg)](http://terma.com/jenkins/job/ama)",
                new Message(0.85f, 0.5f).forComment(buildUrl, null, 80, 90, true, "develop"));
    }

    @Test
    public void forComment() {
        String buildUrl = "http://terma.com/jenkins/job/ama";
        String jenkinsUrl = "jenkinsUrl";
        Assert.assertEquals(
                "[![100% (0.0%) vs master 100%](jenkinsUrl/coverage-status-icon/?coverage=1.0&masterCoverage=1.0&targetBranch=master)](http://terma.com/jenkins/job/ama)",
                new Message(1, 1).forComment(buildUrl, jenkinsUrl, 0, 0, false, "master"));

        Assert.assertEquals(
                "[![0% (0.0%) vs master 0%](jenkinsUrl/coverage-status-icon/?coverage=0.0&masterCoverage=0.0&targetBranch=master)](http://terma.com/jenkins/job/ama)",
                new Message(0, 0).forComment(buildUrl, jenkinsUrl, 0, 0, false, "master"));

        Assert.assertEquals(
                "[![50% (+50.0%) vs master 0%](jenkinsUrl/coverage-status-icon/?coverage=0.5&masterCoverage=0.0&targetBranch=master)](http://terma.com/jenkins/job/ama)",
                new Message(0.5f, 0).forComment(buildUrl, jenkinsUrl, 0, 0, false, "master"));

        Assert.assertEquals(
                "[![0% (-50.0%) vs staging 50%](jenkinsUrl/coverage-status-icon/?coverage=0.0&masterCoverage=0.5&targetBranch=staging)](http://terma.com/jenkins/job/ama)",
                new Message(0, 0.5f).forComment(buildUrl, jenkinsUrl, 0, 0, false, "staging"));

        Assert.assertEquals(
                "[![70% (+20.0%) vs develop 50%](jenkinsUrl/coverage-status-icon/?coverage=0.7&masterCoverage=0.5&targetBranch=develop)](http://terma.com/jenkins/job/ama)",
                new Message(0.7f, 0.5f).forComment(buildUrl, jenkinsUrl, 0, 0, false, "develop"));
    }

}
