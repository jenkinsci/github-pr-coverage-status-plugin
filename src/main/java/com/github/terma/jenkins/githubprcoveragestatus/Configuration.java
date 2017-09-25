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

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class Configuration extends AbstractDescribableImpl<Configuration> {

    public static final int DEFAULT_YELLOW_THRESHOLD = 80;
    public static final int DEFAULT_GREEN_THRESHOLD = 90;
    public static final boolean DEFAULT_NEGATIVE_COVERAGE_IS_RED = false;

    @Extension
    public static final ConfigurationDescriptor DESCRIPTOR = new ConfigurationDescriptor();

    @DataBoundConstructor
    public Configuration() {
    }

    public static String getSonarUrl() {
        return DESCRIPTOR.getSonarUrl();
    }

    public static String getSonarToken() {
        return DESCRIPTOR.getSonarToken();
    }

    public static String getSonarLogin() {
        return DESCRIPTOR.getSonarLogin();
    }

    public static String getSonarPassword() {
        return DESCRIPTOR.getSonarPassword();
    }

    public static Boolean isUseSonarForMasterCoverage() {
        return DESCRIPTOR.isUseSonarForMasterCoverage();
    }

    public static void setMasterCoverage(final String repo, final float coverage) {
        DESCRIPTOR.set(repo, coverage);
    }

    @Override
    public ConfigurationDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @SuppressWarnings("unused")
    public static final class ConfigurationDescriptor extends Descriptor<Configuration> implements SettingsRepository {

        private static final int DEFAULT_YELLOW_THRESHOLD = 80;
        private static final int DEFAULT_GREEN_THRESHOLD = 90;

        private final Map<String, Float> coverageByRepo = new ConcurrentHashMap<String, Float>();

        private boolean disableSimpleCov;
        private String gitHubApiUrl;
        private String personalAccessToken;
        private String jenkinsUrl;
        private boolean privateJenkinsPublicGitHub;
        private boolean useSonarForMasterCoverage;
        private String sonarUrl;
        private String sonarToken;
        private String sonarLogin;
        private String sonarPassword;

        private int yellowThreshold = DEFAULT_YELLOW_THRESHOLD;
        private int greenThreshold = DEFAULT_GREEN_THRESHOLD;

        public ConfigurationDescriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Coverage status for GitHub Pull Requests";
        }

        @Nonnull
        public Map<String, Float> getCoverageByRepo() {
            return coverageByRepo;
        }

        public void set(String repo, float coverage) {
            coverageByRepo.put(repo, coverage);
            save();
        }

        @Override
        public String getGitHubApiUrl() {
            return gitHubApiUrl;
        }

        @Override
        public String getPersonalAccessToken() {
            return personalAccessToken;
        }

        @Override
        public int getYellowThreshold() {
            return yellowThreshold;
        }

        @Override
        public int getGreenThreshold() {
            return greenThreshold;
        }

        @Override
        public boolean isPrivateJenkinsPublicGitHub() {
            return privateJenkinsPublicGitHub;
        }

        @Override
        public boolean isUseSonarForMasterCoverage() {
            return useSonarForMasterCoverage;
        }

        @Override
        public boolean isDisableSimpleCov() {
            return disableSimpleCov;
        }

        @Override
        public String getSonarUrl() {
            return sonarUrl;
        }

        @Override
        public String getSonarToken() {
            return sonarToken;
        }

        @Override
        public String getJenkinsUrl() {
            return jenkinsUrl;
        }

        public String getSonarLogin() {
            return sonarLogin;
        }

        public String getSonarPassword() {
            return sonarPassword;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            gitHubApiUrl = StringUtils.trimToNull(formData.getString("gitHubApiUrl"));
            personalAccessToken = StringUtils.trimToNull(formData.getString("personalAccessToken"));
            yellowThreshold = NumberUtils.toInt(formData.getString("yellowThreshold"), DEFAULT_YELLOW_THRESHOLD);
            greenThreshold = NumberUtils.toInt(formData.getString("greenThreshold"), DEFAULT_GREEN_THRESHOLD);
            jenkinsUrl = StringUtils.trimToNull(formData.getString("jenkinsUrl"));
            privateJenkinsPublicGitHub = BooleanUtils.toBoolean(formData.getString("privateJenkinsPublicGitHub"));
            useSonarForMasterCoverage = BooleanUtils.toBoolean(formData.getString("useSonarForMasterCoverage"));
            disableSimpleCov = BooleanUtils.toBoolean(formData.getString("disableSimpleCov"));
            sonarUrl = StringUtils.trimToNull(formData.getString("sonarUrl"));
            sonarToken = StringUtils.trimToNull(formData.getString("sonarToken"));
            sonarLogin = StringUtils.trimToNull(formData.getString("sonarLogin"));
            sonarPassword = StringUtils.trimToNull(formData.getString("sonarPassword"));
            save();
            return super.configure(req, formData);
        }

    }

}
