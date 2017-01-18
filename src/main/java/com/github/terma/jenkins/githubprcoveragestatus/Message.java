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

    private static final String BADGE_TEMPLATE = "https://img.shields.io/badge/coverage-%s-%s.svg"; //see http://shields.io/ for reference
    private static final String COLOR_RED = "red";
    private static final String COLOR_YELLOW = "yellow";
    private static final String COLOR_GREEN = "brightgreen";

    private final float coverage;
    private final float masterCoverage;

    public Message(float coverage, float masterCoverage) {
        this.coverage = Percent.roundFourAfterDigit(coverage);
        this.masterCoverage = Percent.roundFourAfterDigit(masterCoverage);
    }

    public String forConsole() {
        return String.format("Coverage %s changed %s vs master %s",
                Percent.toWholeNoSignString(coverage),
                Percent.toString(Percent.change(coverage, masterCoverage)),
                Percent.toWholeNoSignString(masterCoverage));
    }

    public String forComment(final String buildUrl, int yellowThreshold, int greenThreshold) {
        String color = getColor(yellowThreshold, greenThreshold);
        try {
            String badgeUri = String.format(BADGE_TEMPLATE, URIUtil.encodePath(forIcon()), color);
            return "[![" + forIcon() + "](" + badgeUri + ")](" + buildUrl + ")";
        } catch (URIException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private String getColor(int yellowThreshold, int greenThreshold) {
        String color = COLOR_GREEN;
        final int coveragePercent = Percent.of(coverage);
        if (coveragePercent < yellowThreshold) {
            color = COLOR_RED;
        }
        else if (coveragePercent < greenThreshold) {
            color = COLOR_YELLOW;
        }
        return color;
    }

    /**
     * Example: 92% (+23%) vs master 70%
     */
    public String forIcon() {
        return String.format("%s (%s) vs master %s",
                Percent.toWholeNoSignString(coverage),
                Percent.toString(Percent.change(coverage, masterCoverage)),
                Percent.toWholeNoSignString(masterCoverage));
    }
}
