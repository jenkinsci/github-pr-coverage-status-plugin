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

public class JacocoParserTest {

    @Test
    public void extractCoverageFromJacocoReport() throws IOException {
        String filePath = JacocoParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/JacocoParserTest/jacoco.xml").getFile();

        Assert.assertEquals(0.22, new JacocoParser().get(filePath), 0.1);
    }

    @Test
    public void extractCoverageFromJacocoReportWhenNoLinesOfCode() throws IOException {
        String filePath = JacocoParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/JacocoParserTest/jacoco-no-code.xml").getFile();

        Assert.assertEquals(0, new JacocoParser().get(filePath), 0.1);
    }

    @Test
    public void throwExceptionWhenExtractCoverageFromJacocoAndNoLineTag() throws IOException {
        String filePath = JacocoParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/JacocoParserTest/jacoco-no-line-tag.xml").getFile();

        try {
            new JacocoParser().get(filePath);
            Assert.fail("Where is my exception?");
        } catch (Exception e) {
            String messageWithoutAbsolutePath = e.getMessage().replace(filePath, "FILE_PATH");
            Assert.assertEquals(
                    "Strange Jacoco report!\n" +
                            "File path: FILE_PATH\n" +
                            "Can't extract float value by XPath: /report/counter[@type='LINE']/@missed\n" +
                            "from:\n" +
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><!DOCTYPE report PUBLIC \"-//JACOCO//DTD Report 1.0//EN\"\n" +
                            "        \"report.dtd\">\n" +
                            "<report name=\"GitHub Pull Request Coverage Status\">\n" +
                            "</report>",
                    messageWithoutAbsolutePath);
        }
    }

    @Test
    public void throwExceptionWhenExtractCoverageFromJacocoAndMissedNotNumber() throws IOException {
        String filePath = JacocoParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/JacocoParserTest/jacoco-missed-not-number.xml").getFile();

        try {
            new JacocoParser().get(filePath);
            Assert.fail("Where is my exception?");
        } catch (Exception e) {
            String messageWithoutAbsolutePath = e.getMessage().replace(filePath, "FILE_PATH");
            Assert.assertEquals(
                    "Strange Jacoco report!\n" +
                            "File path: FILE_PATH\n" +
                            "Can't extract float value by XPath: /report/counter[@type='LINE']/@missed\n" +
                            "from:\n" +
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><!DOCTYPE report PUBLIC \"-//JACOCO//DTD Report 1.0//EN\"\n" +
                            "        \"report.dtd\">\n" +
                            "<report name=\"GitHub Pull Request Coverage Status\">\n" +
                            "    <counter type=\"LINE\" missed=\"X\" covered=\"0\"/>\n" +
                            "</report>",
                    messageWithoutAbsolutePath);
        }
    }

    @Test
    public void throwExceptionWhenExtractCoverageFromJacocoAndCoveredNotNumber() throws IOException {
        String filePath = JacocoParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/JacocoParserTest/jacoco-covered-not-number.xml").getFile();

        try {
            new JacocoParser().get(filePath);
            Assert.fail("Where is my exception?");
        } catch (Exception e) {
            String messageWithoutAbsolutePath = e.getMessage().replace(filePath, "FILE_PATH");
            Assert.assertEquals(
                    "Strange Jacoco report!\n" +
                            "File path: FILE_PATH\n" +
                            "Can't extract float value by XPath: /report/counter[@type='LINE']/@covered\n" +
                            "from:\n" +
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><!DOCTYPE report PUBLIC \"-//JACOCO//DTD Report 1.0//EN\"\n" +
                            "        \"report.dtd\">\n" +
                            "<report name=\"GitHub Pull Request Coverage Status\">\n" +
                            "    <counter type=\"LINE\" missed=\"0\" covered=\"X\"/>\n" +
                            "</report>",
                    messageWithoutAbsolutePath);
        }
    }

    @Test
    public void throwExceptionWhenExtractCoverageFromJacocoAndNoFile() throws IOException {
        try {
            new JacocoParser().get("/jacoco-no-file.xml");
            Assert.fail("Where is my exception?");
        } catch (Exception e) {
            Assert.assertEquals("Can't read Jacoco report by path: /jacoco-no-file.xml", e.getMessage());
        }
    }

}
