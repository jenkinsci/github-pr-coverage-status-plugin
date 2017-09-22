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

import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageTest {

    Pattern colorPattern = Pattern.compile("color=([a-z]+)");

    @Test
    public void buildNiceForConsole() {
        Assert.assertEquals("Coverage 100% changed 0.0% vs master 100%", new Message(1, 1).forConsole());
        Assert.assertEquals("Coverage 0% changed 0.0% vs master 0%", new Message(0, 0).forConsole());
        Assert.assertEquals("Coverage 50% changed +50.0% vs master 0%", new Message(0.5f, 0).forConsole());
        Assert.assertEquals("Coverage 0% changed -50.0% vs master 50%", new Message(0, 0.5f).forConsole());
        Assert.assertEquals("Coverage 70% changed +20.0% vs master 50%", new Message(0.7f, 0.5f).forConsole());
        Assert.assertEquals("Coverage 0% changed +0.02% vs master 0%", new Message(0.0007f, 0.0005f).forConsole());
        Assert.assertEquals("Coverage 0% changed 0.0% vs master 0%", new Message(0.000007f, 0.000005f).forConsole());
    }

    @Test
    public void buildNiceForIcon() {
        Assert.assertEquals("100% (0.0%) vs master 100%", new Message(1, 1).forIcon());
        Assert.assertEquals("0% (0.0%) vs master 0%", new Message(0, 0).forIcon());
        Assert.assertEquals("50% (+50.0%) vs master 0%", new Message(0.5f, 0).forIcon());
        Assert.assertEquals("0% (-50.0%) vs master 50%", new Message(0, 0.5f).forIcon());
        Assert.assertEquals("70% (+20.0%) vs master 50%", new Message(0.7f, 0.5f).forIcon());
        Assert.assertEquals("69% (-0.7%) vs master 69%", new Message(0.686f, 0.693f).forIcon());
        Assert.assertEquals("60% (+0.06%) vs master 60%", new Message(0.6007f, 0.6001f).forIcon());
        Assert.assertEquals("0% (+0.01%) vs master 0%", new Message(0.00007f, 0.00001f).forIcon());
        Assert.assertEquals("0% (0.0%) vs master 0%", new Message(0.000007f, 0.000001f).forIcon());
    }

    @Test
    public void forCommentWithShieldIo() {
        String buildUrl = "http://terma.com/jenkins/job/ama";
        Assert.assertEquals(
                "[![100% (0.0%) vs master 100%](https://img.shields.io/badge/coverage-100%25%20(0.0%25)%20vs%20master%20100%25-brightgreen.svg)](http://terma.com/jenkins/job/ama)",
            new Message(1, 1).forComment(buildUrl, null, 80, 90, false, true));

        Assert.assertEquals(
                "[![0% (0.0%) vs master 0%](https://img.shields.io/badge/coverage-0%25%20(0.0%25)%20vs%20master%200%25-red.svg)](http://terma.com/jenkins/job/ama)",
            new Message(0, 0).forComment(buildUrl, null, 80, 90, false, true));

        Assert.assertEquals(
                "[![50% (+50.0%) vs master 0%](https://img.shields.io/badge/coverage-50%25%20(%2B50.0%25)%20vs%20master%200%25-red.svg)](http://terma.com/jenkins/job/ama)",
            new Message(0.5f, 0).forComment(buildUrl, null, 80, 90, false, true));

        Assert.assertEquals(
                "[![0% (-50.0%) vs master 50%](https://img.shields.io/badge/coverage-0%25%20(--50.0%25)%20vs%20master%2050%25-red.svg)](http://terma.com/jenkins/job/ama)",
            new Message(0, 0.5f).forComment(buildUrl, null, 80, 90, false, true));

        Assert.assertEquals(
                "[![85% (+35.0%) vs master 50%](https://img.shields.io/badge/coverage-85%25%20(%2B35.0%25)%20vs%20master%2050%25-yellow.svg)](http://terma.com/jenkins/job/ama)",
            new Message(0.85f, 0.5f).forComment(buildUrl, null, 80, 90, false, true));
    }

    @Test
    public void forComment() {
        String buildUrl = "http://terma.com/jenkins/job/ama";
        String jenkinsUrl = "jenkinsUrl";
        Assert.assertEquals(
            "[![100% (0.0%) vs master 100%](jenkinsUrl/" + CoverageStatusIconAction.URL_NAME
                + "/?coverage=1.0&masterCoverage=1.0&color=brightgreen)](http://terma.com/jenkins/job/ama)",
            new Message(1, 1).forComment(buildUrl, jenkinsUrl, 0, 0, false, false));

        Assert.assertEquals(
            "[![0% (0.0%) vs master 0%](jenkinsUrl/" + CoverageStatusIconAction.URL_NAME
                + "/?coverage=0.0&masterCoverage=0.0&color=brightgreen)](http://terma.com/jenkins/job/ama)",
            new Message(0, 0).forComment(buildUrl, jenkinsUrl, 0, 0, false, false));

        Assert.assertEquals(
            "[![50% (+50.0%) vs master 0%](jenkinsUrl/" + CoverageStatusIconAction.URL_NAME
                + "/?coverage=0.5&masterCoverage=0.0&color=brightgreen)](http://terma.com/jenkins/job/ama)",
            new Message(0.5f, 0).forComment(buildUrl, jenkinsUrl, 0, 0, false, false));

        Assert.assertEquals(
            "[![0% (-50.0%) vs master 50%](jenkinsUrl/" + CoverageStatusIconAction.URL_NAME
                + "/?coverage=0.0&masterCoverage=0.5&color=brightgreen)](http://terma.com/jenkins/job/ama)",
            new Message(0, 0.5f).forComment(buildUrl, jenkinsUrl, 0, 0, false, false));

        Assert.assertEquals(
            "[![70% (+20.0%) vs master 50%](jenkinsUrl/" + CoverageStatusIconAction.URL_NAME
                + "/?coverage=0.7&masterCoverage=0.5&color=brightgreen)](http://terma.com/jenkins/job/ama)",
            new Message(0.7f, 0.5f).forComment(buildUrl, jenkinsUrl, 0, 0, false, false));
    }

    @Test
    public void verifyNegativeCoverageIsRed_expectColorChange() {
        String buildUrl = "http://terma.com/jenkins/job/ama";
        String jenkinsUrl = "jenkinsUrl";

        final int YELLOWTHRESHOLD = 50;
        final int GREENTHRESHOLD = 80;

        // Coverage increases
        final String colorCoverageIncrease = extractColor(
            new Message(0.7f, 0.6f).forComment(buildUrl, jenkinsUrl, YELLOWTHRESHOLD, GREENTHRESHOLD, true, false));
        Assert.assertEquals("yellow", colorCoverageIncrease);

        // Coverage decreases but is still over yellow threshold
        final String colorCoverageDecrease = extractColor(
            new Message(0.55f, 0.6f).forComment(buildUrl, jenkinsUrl, YELLOWTHRESHOLD, GREENTHRESHOLD, true, false));
        Assert.assertEquals("red", colorCoverageDecrease);
    }

    @Test
    public void verifyNegativeCoverageIsRed_expectGreenEvenOnDecrease() {
        String buildUrl = "http://terma.com/jenkins/job/ama";
        String jenkinsUrl = "jenkinsUrl";

        final int YELLOWTHRESHOLD = 50;
        final int GREENTHRESHOLD = 80;

        final String colorCoverageDecrease = extractColor(
            new Message(0.89f, 0.9f).forComment(buildUrl, jenkinsUrl, YELLOWTHRESHOLD, GREENTHRESHOLD, true, false));
        Assert.assertEquals("brightgreen", colorCoverageDecrease);
    }

    private String extractColor(String message) {
        final Matcher matcher = colorPattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new InputMismatchException("Unable to extract color=.... from message: " + message);
        }
    }

}
