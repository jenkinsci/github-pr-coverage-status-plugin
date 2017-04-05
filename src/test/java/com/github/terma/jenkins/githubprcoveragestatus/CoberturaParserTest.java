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

import java.io.IOException;

public class CoberturaParserTest {

    @Test
    public void extractCoverageFromCoberturaReportAsLineRatePlusBranchRateDivByTwo() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura.xml").getFile();

        Assert.assertEquals(0.94, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void extractZeroCoverageIfNoZeroLineRateAndBranchRate() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-zero-coverage.xml").getFile();

        Assert.assertEquals(0, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void extractCoverageIfBranchRateIsZeroAndHasOnlyLineRate() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-zero-branch-rate.xml").getFile();

        Assert.assertEquals(0.5, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void extractCoverageIfLineRateIsZeroAndHasBranchRate() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-zero-line-rate.xml").getFile();

        Assert.assertEquals(1, new CoberturaParser().get(filePath), 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionIfNoLineRate() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-no-line-rate.xml").getFile();
        new CoberturaParser().get(filePath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionIfNoBranchRate() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-no-branch-rate.xml").getFile();
        new CoberturaParser().get(filePath);
    }

}
