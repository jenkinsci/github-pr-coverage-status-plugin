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

public class CloverParserTest {

    @Test
    public void extractCoverageFromCloverReport() {
        String filePath = CloverParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CloverParserTest/clover.xml").getFile();

        Assert.assertEquals(0.5, new CloverParser().get(filePath), 0.1);
    }

    @Test
    public void extractZeroCoverageIfNoCoveredStatements() {
        String filePath = CloverParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CloverParserTest/clover-zero-statements-coverage.xml").getFile();

        Assert.assertEquals(0, new CloverParser().get(filePath), 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionIfReportDoesntHaveStatementsAttribute() {
        String filePath = CloverParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CloverParserTest/clover-invalid-no-statements.xml").getFile();

        Assert.assertEquals(0, new CloverParser().get(filePath), 0.1);
    }

    @Test
    public void extractZeroCoverageIfZeroStatements() {
        String filePath = CloverParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CloverParserTest/clover-zero-statements.xml").getFile();

        Assert.assertEquals(0, new CloverParser().get(filePath), 0.1);
    }

}
