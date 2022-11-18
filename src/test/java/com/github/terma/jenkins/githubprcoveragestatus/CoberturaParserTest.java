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

public class CoberturaParserTest {

    @Test
    public void extractCoverageFromCoberturaReportAsLineRatePlusBranchRateDivByTwo() {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura.xml").getFile();

        Assert.assertEquals(0.94, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void extractCoverageFromCoberturaReportWithSingleQuotes() {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-with-single-quotes.xml").getFile();

        Assert.assertEquals(0.94, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void extractZeroCoverageIfNoZeroLineRateAndBranchRate() {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-zero-coverage.xml").getFile();

        Assert.assertEquals(0, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void extractCoverageIfBranchRateIsZeroAndHasOnlyLineRate() {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-zero-branch-rate.xml").getFile();

        Assert.assertEquals(0.5, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void extractCoverageIfLineRateIsZeroAndHasBranchRate() {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-zero-line-rate.xml").getFile();

        Assert.assertEquals(1, new CoberturaParser().get(filePath), 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionIfNoLineRate() {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-no-line-rate.xml").getFile();
        new CoberturaParser().get(filePath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionIfNoBranchRate() {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-no-branch-rate.xml").getFile();
        new CoberturaParser().get(filePath);
    }

}
