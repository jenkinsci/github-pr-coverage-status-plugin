package com.github.terma.jenkins.githubprcoveragestatus;

import com.jayway.jsonpath.JsonPathException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * <a href="https://github.com/vicentllongo/simplecov-json">SimpleCov JSON</a>
 */
public class SimpleCovParser implements CoverageReportParser {

    private static final String METRIC_PATH = "$.metrics.covered_percent";

    @Override
    public boolean canAggregate() {
      return false;
    }
    @Override
    public float getAggregate() {
      throw new UnsupportedOperationException();
    }

    @Override
    public float get(String simpleCovFilePath) {
        final String content;
        try {
            content = FileUtils.readFileToString(new File(simpleCovFilePath));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Can't read SimpleCov report by path: " + simpleCovFilePath);
        }

        Double covered = extractValueFromPath(content);
        return covered.floatValue() / 100;
    }

    private Double extractValueFromPath(String content) {
        try {
            return JsonUtils.findInJson(content, METRIC_PATH);
        } catch (JsonPathException error) {
            throw new IllegalArgumentException("Strange SimpleCov report!\n" +
                    "Can't extract float value by JsonPath: " + METRIC_PATH + "\n" +
                    "from:\n" + content);
        }
    }
}
