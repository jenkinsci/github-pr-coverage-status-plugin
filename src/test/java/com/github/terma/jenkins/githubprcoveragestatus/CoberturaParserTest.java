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
    public void extractCoverageFromCoberturaReport() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura.xml").getFile();

        Assert.assertEquals(0.94, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void extractZeroCoverageIfNoCoveredLinesAndBranches() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-zero-coverage.xml").getFile();

        Assert.assertEquals(0, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void extractOnlyLineCoverageIfZeroBranchesCovered() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-zero-branches-covered.xml").getFile();

        Assert.assertEquals(0.25, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void extractOnlyLineCoverageIfZeroLinesCovered() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-zero-lines-covered.xml").getFile();

        Assert.assertEquals(0.25, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void ignoreLinesCoverageIfValidLinesNotPresent() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-no-lines-valid.xml").getFile();

        Assert.assertEquals(1, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void ignoreLinesCoverageIfValidBranchesNotPresent() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-no-branches-valid.xml").getFile();

        Assert.assertEquals(0.5, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void ignoreLinesCoverageIfZeroLinesValid() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-zero-lines-valid.xml").getFile();

        Assert.assertEquals(1, new CoberturaParser().get(filePath), 0.1);
    }

    @Test
    public void ignoreLinesCoverageIfZeroBranchesValid() throws IOException {
        String filePath = CoberturaParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CoberturaParserTest/cobertura-zero-branches-valid.xml").getFile();

        Assert.assertEquals(1, new CoberturaParser().get(filePath), 0.1);
    }

}
