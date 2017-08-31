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

public class ServiceRegistry {

    private static MasterCoverageRepository masterCoverageRepository;
    private static CoverageRepository coverageRepository;
    private static SettingsRepository settingsRepository;
    private static PullRequestRepository pullRequestRepository;

    public static MasterCoverageRepository getMasterCoverageRepository(PrintStream buildLog, final String login, final String password) {
        if (masterCoverageRepository != null) return masterCoverageRepository;

        if (Configuration.isUseSonarForMasterCoverage()) {
            final String sonarUrl = Configuration.getSonarUrl();
            if (login != null && password != null) {
                buildLog.println("take master coverage from sonar by login/password");
                return new SonarMasterCoverageRepository(sonarUrl, login, password, buildLog);
            }
            if (Configuration.getSonarToken() != null) {
                buildLog.println("take master coverage from sonar by token");
                return new SonarMasterCoverageRepository(sonarUrl, Configuration.getSonarToken(), "", buildLog);
            }
            buildLog.println("take master coverage from sonar by login/password");
            return new SonarMasterCoverageRepository(sonarUrl, Configuration.getSonarLogin(), Configuration.getSonarPassword(), buildLog);
        } else {
            buildLog.println("use default coverage repo");
            return new BuildMasterCoverageRepository(buildLog);
        }
    }

    public static void setMasterCoverageRepository(MasterCoverageRepository masterCoverageRepository) {
        ServiceRegistry.masterCoverageRepository = masterCoverageRepository;
    }

    public static CoverageRepository getCoverageRepository(final boolean disableSimpleCov) {
        return coverageRepository != null ? coverageRepository : new GetCoverageCallable(disableSimpleCov);
    }

    public static void setCoverageRepository(CoverageRepository coverageRepository) {
        ServiceRegistry.coverageRepository = coverageRepository;
    }

    public static SettingsRepository getSettingsRepository() {
        return settingsRepository != null ? settingsRepository : Configuration.DESCRIPTOR;
    }

    public static void setSettingsRepository(SettingsRepository settingsRepository) {
        ServiceRegistry.settingsRepository = settingsRepository;
    }

    public static PullRequestRepository getPullRequestRepository() {
        return pullRequestRepository != null ? pullRequestRepository : new GitHubPullRequestRepository();
    }

    public static void setPullRequestRepository(PullRequestRepository pullRequestRepository) {
        ServiceRegistry.pullRequestRepository = pullRequestRepository;
    }
}
