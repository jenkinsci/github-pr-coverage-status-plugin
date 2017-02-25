package com.github.terma.jenkins.githubprcoveragestatus;

import com.github.terma.jenkins.githubcoverageupdater.JsonUtils;
import com.jayway.jsonpath.JsonPathException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class SimpleCovParser implements CoverageReportParser {

    private static final String METRIC_PATH = "$.metrics.covered_percent";

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
        return covered.floatValue();
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
