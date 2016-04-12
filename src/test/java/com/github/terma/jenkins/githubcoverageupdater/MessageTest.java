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
package com.github.terma.jenkins.githubcoverageupdater;

import org.junit.Assert;
import org.junit.Test;

public class MessageTest {

    @Test
    public void buildNiceForConsole() {
        Assert.assertEquals("Coverage 100% changed +0% vs master 100%", new Message(1, 1).forConsole());
        Assert.assertEquals("Coverage 0% changed +0% vs master 0%", new Message(0, 0).forConsole());
        Assert.assertEquals("Coverage 50% changed +50% vs master 0%", new Message(0.5f, 0).forConsole());
        Assert.assertEquals("Coverage 0% changed -50% vs master 50%", new Message(0, 0.5f).forConsole());
        Assert.assertEquals("Coverage 70% changed +20% vs master 50%", new Message(0.7f, 0.5f).forConsole());
    }

    @Test
    public void buildNiceForIcon() {
        Assert.assertEquals("100% (+0%) vs master 100%", new Message(1, 1).forIcon());
        Assert.assertEquals("0% (+0%) vs master 0%", new Message(0, 0).forIcon());
        Assert.assertEquals("50% (+50%) vs master 0%", new Message(0.5f, 0).forIcon());
        Assert.assertEquals("0% (-50%) vs master 50%", new Message(0, 0.5f).forIcon());
        Assert.assertEquals("70% (+20%) vs master 50%", new Message(0.7f, 0.5f).forIcon());
    }

    @Test
    public void buildNiceForComment() {
        String jenkinsUrl = "http://terma.com/jenkins/jobs/ama";
        Assert.assertEquals(
                "[![Coverage](http://terma.com/jenkins/coverage-status-icon?coverage=1.0&masterCoverage=1.0)](http://terma.com/jenkins/jobs/ama)",
                new Message(1, 1).forComment(jenkinsUrl));

        Assert.assertEquals(
                "[![Coverage](http://terma.com/jenkins/coverage-status-icon?coverage=0.0&masterCoverage=0.0)](http://terma.com/jenkins/jobs/ama)",
                new Message(0, 0).forComment(jenkinsUrl));

        Assert.assertEquals(
                "[![Coverage](http://terma.com/jenkins/coverage-status-icon?coverage=0.5&masterCoverage=0.0)](http://terma.com/jenkins/jobs/ama)",
                new Message(0.5f, 0).forComment(jenkinsUrl));

        Assert.assertEquals(
                "[![Coverage](http://terma.com/jenkins/coverage-status-icon?coverage=0.0&masterCoverage=0.5)](http://terma.com/jenkins/jobs/ama)",
                new Message(0, 0.5f).forComment(jenkinsUrl));

        Assert.assertEquals(
                "[![Coverage](http://terma.com/jenkins/coverage-status-icon?coverage=0.7&masterCoverage=0.5)](http://terma.com/jenkins/jobs/ama)",
                new Message(0.7f, 0.5f).forComment(jenkinsUrl));
    }

}
