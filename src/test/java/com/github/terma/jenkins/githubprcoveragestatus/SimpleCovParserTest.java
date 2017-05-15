package com.github.terma.jenkins.githubprcoveragestatus;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;


public class SimpleCovParserTest {

    @Test
    public void extractsCoverageFromSimpleCovReport() {
        String filePath = SimpleCovParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/SimpleCovParserTest/coverage.json").getFile();

        float coverage = new SimpleCovParser().get(filePath);

        // won't be an exact match as we're converting double to float
        assertThat((double) coverage, is(closeTo(0.857142857142857, 0.00001)));

    }

    @Test
    public void errorsReadingNoExistantFile() {
        try {
            float coverage = new SimpleCovParser().get("wibble/wobble.not_here");
            TestCase.fail("should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(equalTo("Can't read SimpleCov report by path: wibble/wobble.not_here")));
        }
    }

    @Test
    public void notPresentPercentButValidJson() {
        String filePath = SimpleCovParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/SimpleCovParserTest/coverage_no_covered_percent.json").getFile();

        try {
            float coverage = new SimpleCovParser().get(filePath);
            TestCase.fail("should not reach here");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), startsWith("Strange SimpleCov report!\nCan't extract float value by JsonPath:"));
        }
    }

    @Test
    public void inValidJson() {
        String filePath = SimpleCovParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/SimpleCovParserTest/coverage_invalid.json").getFile();

        try {
            float coverage = new SimpleCovParser().get(filePath);
            TestCase.fail("should not reach here");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), startsWith("Strange SimpleCov report!\nCan't extract float value by JsonPath:"));
        }
    }

}