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

/**
 * For more info about Clover see:
 * <a href="https://answers.atlassian.com/questions/203628/clover-xml-number-of-covered-lines>
 * https://answers.atlassian.com/questions/203628/clover-xml-number-of-covered-lines</a>
 * <a href="https://wiki.jenkins-ci.org/display/JENKINS/Clover+Plugin>
 * https://wiki.jenkins-ci.org/display/JENKINS/Clover+Plugin</a>
 * <a href="https://phpunit.de/manual/current/en/logging.html#logging.codecoverage.xml"
 * https://phpunit.de/manual/current/en/logging.html#logging.codecoverage.xml</a>
 */
class CloverParser implements CoverageReportParser {

    private static final String TOTAL_STATEMENTS_XPATH = "/coverage/project/metrics/@statements";
    private static final String COVER_STATEMENTS_XPATH = "/coverage/project/metrics/@coveredstatements";

    @Override
    public boolean canAggregate() {
      return false;
    }
    @Override
    public float getAggregate() {
      throw new UnsupportedOperationException();
    }

    private int getByXpath(final String filePath, final String content, final String xpath) {
        try {
            return Integer.parseInt(XmlUtils.findInXml(content, xpath));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Strange Clover report!\n" +
                            "File path: " + filePath + "\n" +
                            "Can't extract float value by XPath: " + xpath + "\n" +
                            "from:\n" + content, e);
        }
    }

    @Override
    public float get(final String cloverFilePath) {
        final String content;
        try {
            content = FileUtils.readFileToString(new File(cloverFilePath));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Can't read Clover report by path: " + cloverFilePath);
        }

        final float statements = getByXpath(cloverFilePath, content, TOTAL_STATEMENTS_XPATH);
        final float coveredStatements = getByXpath(cloverFilePath, content, COVER_STATEMENTS_XPATH);

        if (statements == 0) return 0;
        else return coveredStatements / statements;
    }

}
