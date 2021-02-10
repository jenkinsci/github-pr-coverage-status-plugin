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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.commons.httpclient.HttpStatus.SC_BAD_REQUEST;

@SuppressWarnings("WeakerAccess")
public class SonarMasterCoverageRepository implements MasterCoverageRepository {

    private static final String SONAR_SEARCH_PROJECTS_API_PATH = "/api/projects/index";
    private static final String SONAR_COMPONENT_MEASURE_API_PATH = "/api/measures/component";
    public static final String SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME = "coverage";

    private final String sonarUrl;
    private final String login;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper().disable(FAIL_ON_UNKNOWN_PROPERTIES);
    private PrintStream buildLog;

    public SonarMasterCoverageRepository(String sonarUrl, String login, String password, PrintStream buildLog) {
        this.sonarUrl = sonarUrl;
        this.login = login;
        this.buildLog = buildLog;
        httpClient = new HttpClient();
        if (login != null) {
            httpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
        }
    }

    @Override
    public float get(final String gitHubRepoUrl) {
        final String repoName = GitUtils.getRepoName(gitHubRepoUrl);
        log("Getting coverage for Git Repo URL: %s by repo name: %s", gitHubRepoUrl, repoName);
        try {
            final SonarProject sonarProject = getSonarProject(repoName);
            return getCoverageMeasure(sonarProject);
        } catch (Exception e) {
            log("Failed to get master coverage for %s", gitHubRepoUrl);
            log("Exception message '%s'", e);
            e.printStackTrace(buildLog);
            return 0;
        }
    }

    /**
     * Try to find the project in sonarqube based on the repo name from the git uri
     *
     * @return the sonar project found in case multiple are found, the first one is returned
     * @throws SonarProjectRetrievalException if no project could be found or an error occurred during retrieval
     */
    private SonarProject getSonarProject(final String repoName) throws SonarProjectRetrievalException {
        try {
            final String searchUri = sonarUrl + SONAR_SEARCH_PROJECTS_API_PATH + "?search=" + repoName;
            final GetMethod method = executeGetRequest(searchUri);
            final List<SonarProject> sonarProjects = objectMapper.readValue(method.getResponseBodyAsStream(), new TypeReference<List<SonarProject>>() {
            });

            if (sonarProjects.isEmpty()) {
                throw new SonarProjectRetrievalException("No sonar project found for repo" + repoName);
            } else if (sonarProjects.size() == 1) {
                log("Found project for repo name %s - %s", repoName, sonarProjects.get(0));
                return sonarProjects.get(0);
            } else {
                log("Found multiple projects for repo name %s - found %s - returning first result", repoName, sonarProjects);
                return sonarProjects.get(0);
            }
        } catch (final Exception e) {
            throw new SonarProjectRetrievalException(String.format("failed to search for sonar project %s - %s", repoName, e.getMessage()), e);
        }
    }

    /**
     * Try to find code coverage measure for project in sonarqube
     *
     * @return the coverage found for the project
     * @throws SonarCoverageMeasureRetrievalException if an error occurred during retrieval of the coverage
     */
    private float getCoverageMeasure(SonarProject project) throws SonarCoverageMeasureRetrievalException {
        final String uri = MessageFormat.format("{0}{1}?componentKey={2}&metricKeys={3}", sonarUrl, SONAR_COMPONENT_MEASURE_API_PATH, URLEncoder.encode(project.getKey()), SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME);
        try {
            final GetMethod method = executeGetRequest(uri);
            String value = JsonUtils.findInJson(method.getResponseBodyAsString(), "component.measures[0].value");
            return Float.parseFloat(value) / 100;
        } catch (Exception e) {
            throw new SonarCoverageMeasureRetrievalException(String.format("failed to get coverage measure for sonar project %s - %s", project.getKey(), e.getMessage()), e);
        }
    }

    private GetMethod executeGetRequest(String uri) throws IOException, HttpClientException {
        final GetMethod method = new GetMethod(uri);
        if (login != null) {
            method.getHostAuthState().setAuthScheme(new BasicScheme());
        }
        int status = httpClient.executeMethod(method);
        if (status >= SC_BAD_REQUEST) {
            throw new HttpClientException(uri, status, method.getResponseBodyAsString());
        }
        return method;
    }

    private void log(String format, Object... arguments) {
        buildLog.printf(format, arguments);
        buildLog.println();
    }

    private static class HttpClientException extends Exception {
        HttpClientException(String uri, int status, String reason) {
            super("request to " + uri + " failed with " + status + " reason " + reason);
        }
    }

    private static class SonarProject {
        @JsonProperty("k")
        String key;

        String getKey() {
            return key;
        }

        void setKey(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return MessageFormat.format("({0}(key = {1}))", this.getClass().getSimpleName(), key);
        }
    }

    private static class SonarProjectRetrievalException extends Exception {
        private SonarProjectRetrievalException(String message) {
            super(message);
        }

        private SonarProjectRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class SonarCoverageMeasureRetrievalException extends Exception {

        private SonarCoverageMeasureRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
