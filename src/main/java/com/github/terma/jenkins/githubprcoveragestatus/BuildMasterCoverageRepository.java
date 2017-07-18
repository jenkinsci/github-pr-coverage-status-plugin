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

import java.io.PrintStream;

public class BuildMasterCoverageRepository implements MasterCoverageRepository {

    private final PrintStream buildLog;

    public BuildMasterCoverageRepository(final PrintStream buildLog) {
        this.buildLog = buildLog;
    }

    @Override
    public float get(final String gitHubRepoUrl) {
        if (gitHubRepoUrl == null) return 0;
        final Float coverage = Configuration.DESCRIPTOR.getCoverageByRepo().get(gitHubRepoUrl);
        if (coverage == null) {
            buildLog.println("Can't find master coverage repository: " + gitHubRepoUrl
                    + " in stored: " + Configuration.DESCRIPTOR.getCoverageByRepo() + "\n"
                    + "Make sure that you have run build with step: " + MasterCoverageAction.DISPLAY_NAME);
            return 0;
        }
        return coverage;
    }

}
