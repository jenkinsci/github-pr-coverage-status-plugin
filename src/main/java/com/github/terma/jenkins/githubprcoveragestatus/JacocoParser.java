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

/*
<counter type="INSTRUCTION" missed="1" covered="4"/>
    <counter type="LINE" missed="1" covered="2"/>
    <counter type="COMPLEXITY" missed="1" covered="2"/>
    <counter type="METHOD" missed="1" covered="2"/>
    <counter type="CLASS" missed="0" covered="1"/>
 */
class JacocoParser implements CoverageReportParser {

    private static final String MISSED_INSTRUCTION_XPATH = "/report/counter[@type='INSTRUCTION']/@missed";
    private static final String COVERAGE_INSTRUCTION_XPATH = "/report/counter[@type='INSTRUCTION']/@covered";
    private static final String MISSED_LINE_XPATH = "/report/counter[@type='LINE']/@missed";
    private static final String COVERAGE_LINE_XPATH = "/report/counter[@type='LINE']/@covered";
    private static final String MISSED_BRANCH_XPATH = "/report/counter[@type='BRANCH']/@missed";
    private static final String COVERAGE_BRANCH_XPATH = "/report/counter[@type='BRANCH']/@covered";

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
    public float get(final String jacocoFilePath) {
        final String content;
        try {
            content = FileUtils.readFileToString(new File(jacocoFilePath));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Can't read Jacoco report by path: " + jacocoFilePath);
        }

        final SettingsRepository settingsRepository = ServiceRegistry.getSettingsRepository();
        String missedMetric;
        String coverageMetric;
        if (settingsRepository.getSonarCoverageMetric() != null && SonarMasterCoverageRepository.SONAR_OVERALL_INSTRUCTION_COVERAGE_METRIC_NAME.equalsIgnoreCase(settingsRepository.getSonarCoverageMetric())) {
            missedMetric = MISSED_INSTRUCTION_XPATH;
            coverageMetric = COVERAGE_INSTRUCTION_XPATH;
        } else if (settingsRepository.getSonarCoverageMetric() != null && SonarMasterCoverageRepository.SONAR_OVERALL_BRANCH_COVERAGE_METRIC_NAME.equalsIgnoreCase(settingsRepository.getSonarCoverageMetric())) {
            missedMetric = MISSED_BRANCH_XPATH;
            coverageMetric = COVERAGE_BRANCH_XPATH;
        } else {
            missedMetric = MISSED_LINE_XPATH;
            coverageMetric = COVERAGE_LINE_XPATH;
        }
        
        final float countMissed = getByXpath(jacocoFilePath, content, missedMetric);
        final float countCovered = getByXpath(jacocoFilePath, content, coverageMetric);
        final float count = countCovered + countMissed;
        if (count == 0) {
            return 0;
        } else {
            return countCovered / (count);
        }
    }

}
