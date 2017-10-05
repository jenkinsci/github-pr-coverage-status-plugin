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

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

@SuppressWarnings("WeakerAccess")
class Message {

    //see http://shields.io/ for reference
    private static final String BADGE_TEMPLATE = "https://img.shields.io/badge/coverage-%s-%s.svg";

    private static final String COLOR_RED = "red";
    private static final String COLOR_YELLOW = "yellow";
    private static final String COLOR_GREEN = "brightgreen";

    private final float coverage;
    private final float masterCoverage;
    private final String branchName;

    public Message(float coverage, float masterCoverage, String branchName) {
        this.coverage = Percent.roundFourAfterDigit(coverage);
        this.masterCoverage = Percent.roundFourAfterDigit(masterCoverage);
        this.branchName = branchName;
    }

    public String forConsole() {
        return String.format("Coverage %s changed %s vs %s %s",
                Percent.toWholeNoSignString(coverage),
                Percent.toString(Percent.change(coverage, masterCoverage)),
            branchName,
                Percent.toWholeNoSignString(masterCoverage));
    }

    public String forComment(
            final String buildUrl, final String jenkinsUrl,
            final int yellowThreshold, final int greenThreshold,
        final boolean negativeCoverageIsRed,
            final boolean useShieldsIo) {
        final String icon = forIcon();
        final String color = getColor(yellowThreshold, greenThreshold, negativeCoverageIsRed);

        if (useShieldsIo) {
            return "[![" + icon + "](" + shieldIoUrl(icon, color) + ")](" + buildUrl + ")";
        } else {
            return "[![" + icon + "](" + jenkinsUrl + "/" + CoverageStatusIconAction.URL_NAME + "/" +
                    "?coverage=" + coverage +
                    "&masterCoverage=" + masterCoverage +
                "&color=" + color +
                "&branch=" + branchName +
                    ")](" + buildUrl + ")";
        }
    }

    private String shieldIoUrl(String icon, final String color) {
        // dash should be encoded as two dash
        icon = icon.replace("-", "--");
        try {
            return String.format(BADGE_TEMPLATE, URIUtil.encodePath(icon), color);
        } catch (URIException e) {
            throw new RuntimeException(e);
        }
    }

    private String getColor(int yellowThreshold, int greenThreshold, boolean negativeCoverageIsRed) {
        String color = COLOR_GREEN;
        final int coveragePercent = Percent.of(coverage);
        if (negativeCoverageIsRed && (coverage < masterCoverage) && (coveragePercent < greenThreshold)) {
            color = COLOR_RED;
        } else if (coveragePercent < yellowThreshold) {
            color = COLOR_RED;
        } else if (coveragePercent < greenThreshold) {
            color = COLOR_YELLOW;
        }
        return color;
    }

    /**
     * Example: 92% (+23%) vs master 70%
     */
    public String forIcon() {
        return String.format("%s (%s) vs %s %s",
                Percent.toWholeNoSignString(coverage),
                Percent.toString(Percent.change(coverage, masterCoverage)),
            branchName,
                Percent.toWholeNoSignString(masterCoverage));
    }
}
