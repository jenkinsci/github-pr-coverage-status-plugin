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

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;

public class ServiceRegistry {

    private static MasterCoverageRepository masterCoverageRepository;
    private static CoverageRepository coverageRepository;
    private static BitbucketApi pullRequestRepository;

    public static MasterCoverageRepository getMasterCoverageRepository(PrintStream buildLog, boolean useSonarForMasterCoverage,
        final String sonarUrl, final String sonarLogin, final String sonarPassword, final String sonarToken
    ) {
        if (masterCoverageRepository != null)
            return masterCoverageRepository;

        if (useSonarForMasterCoverage) {
            if (StringUtils.isNotBlank(sonarToken)) {
                buildLog.println("take master coverage from sonar by token");
                return new SonarMasterCoverageRepository(sonarUrl, sonarToken, "", buildLog);
            }
            buildLog.println("take master coverage from sonar by login/password");
            return new SonarMasterCoverageRepository(sonarUrl, sonarLogin, sonarPassword, buildLog);
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

    public static BitbucketApi getPullRequestRepository() {
        return pullRequestRepository;
    }

    public static void setPullRequestRepository(BitbucketApi pullRequestRepository) {
        ServiceRegistry.pullRequestRepository = pullRequestRepository;
    }
}
