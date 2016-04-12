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
package com.github.terma.jenkins.githubcoverageupdater;

@SuppressWarnings("WeakerAccess")
class Message {

    private final float coverage;
    private final float masterCoverage;
    private final float change;

    public Message(float coverage, float masterCoverage) {
        this.coverage = coverage;
        this.masterCoverage = masterCoverage;
        this.change = Percent.change(coverage, masterCoverage);
    }

    public String forConsole() {
        return String.format("Coverage %s changed %s vs master %s",
                Percent.niceNoSign(coverage), Percent.nice(change), Percent.niceNoSign(masterCoverage));
    }

    public String forComment(final String buildUrl) {
        return "[![Coverage](" + Utils.getJenkinsUrlFromBuildUrl(buildUrl) + "coverage-status-icon" +
                "?coverage=" + coverage +
                "&masterCoverage=" + masterCoverage +
                ")](" + buildUrl + ")";
    }

    /**
     * Example: 92% (+23%) vs master 70%
     */
    public String forIcon() {
        return String.format("%s (%s) vs master %s",
                Percent.niceNoSign(coverage), Percent.nice(change), Percent.niceNoSign(masterCoverage));
    }
}
