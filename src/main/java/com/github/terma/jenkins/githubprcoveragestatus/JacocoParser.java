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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
<counter type="INSTRUCTION" missed="1" covered="4"/>
    <counter type="LINE" missed="1" covered="2"/>
    <counter type="COMPLEXITY" missed="1" covered="2"/>
    <counter type="METHOD" missed="1" covered="2"/>
    <counter type="CLASS" missed="0" covered="1"/>
 */
class JacocoParser implements CoverageReportParser {

    private List<String> coverageCounters = new ArrayList<String>() {{
        add("instruction");
        add("complexity");
        add("method");
        add("class");
        add("line");
    }};

    private String coverageCounterType = "";

    public JacocoParser(String coverageCounterType) {
        this.coverageCounterType = coverageCounterType;
    }

    private float getByXpath(final String filePath, final String content, final String xpath) {
        try {
            return Float.parseFloat(XmlUtils.findInXml(content, xpath));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Strange Jacoco report!\n" +
                            "File path: " + filePath + "\n" +
                            "Can't extract float value by XPath: " + xpath + "\n" +
                            "from:\n" + content);
        }
    }

    @Override
    public float get(String jacocoFilePath) {
        final String content;
        try {
            content = FileUtils.readFileToString(new File(jacocoFilePath));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Can't read Jacoco report by path: " + jacocoFilePath);
        }

        if (!isValidCoverageCounter(coverageCounterType)) {
            coverageCounterType = coverageCounters.get(0);
        }

        final float missed = getByXpath(jacocoFilePath, content, getMissedXpath(coverageCounterType));
        final float covered = getByXpath(jacocoFilePath,content, getCoverageXpath(coverageCounterType));
        final float coverage = covered + missed;
        if (coverage == 0) {
            return 0;
        } else {
            return covered / (coverage);
        }
    }

    private boolean isValidCoverageCounter(String coverageCounter) {
        if (coverageCounter == null) {
            return false;
        }
        for (String type : coverageCounters) {
            if (type.equalsIgnoreCase(coverageCounter)) {
                return true;
            }
        }
        return false;
    }

    private String getMissedXpath(String counterType) {
        return "/report/counter[@type='" + counterType.toUpperCase() + "']/@missed";
    }

    private String getCoverageXpath(String counterType) {
        return "/report/counter[@type='" + counterType.toUpperCase() + "']/@covered";
    }
}
