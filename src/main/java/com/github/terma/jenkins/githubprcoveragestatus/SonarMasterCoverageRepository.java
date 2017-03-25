package com.github.terma.jenkins.githubprcoveragestatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.commons.httpclient.HttpStatus.SC_BAD_REQUEST;

public class SonarMasterCoverageRepository implements MasterCoverageRepository {

    private static final String SONAR_SEARCH_PROJECTS_API_PATH = "/api/projects/index";
    private static final String SONAR_COMPONENT_MEASURE_API_PATH = "/api/measures/component";
    private static final String SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME = "overall_line_coverage";

    private final String sonarUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper().disable(FAIL_ON_UNKNOWN_PROPERTIES);
    private PrintStream buildLog;

    public SonarMasterCoverageRepository(String sonarUrl, PrintStream buildLog) {
        this.sonarUrl = sonarUrl;
        this.buildLog = buildLog;
        httpClient = new HttpClient();
    }

    @Override
    public float get(String gitUrl) {
        log("Getting coverage for %s", gitUrl);
        try {
            final SonarProject sonarProject = getSonarProject(gitUrl);
            return getCoverageMeasure(sonarProject);
        } catch (Exception e) {
            log("Failed to get master coverage for %s", gitUrl);
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
    private SonarProject getSonarProject(String gitUrl) throws SonarProjectRetrievalException {
        String repoName = StringUtils.substringAfterLast(StringUtils.removeEnd(gitUrl, ".git"), "/");

        final String searchUri = sonarUrl + SONAR_SEARCH_PROJECTS_API_PATH + "?search=" + repoName;
        try {
            final GetMethod method = executeGetRequest(searchUri);
            List<SonarProject> sonarProjects = objectMapper.readValue(method.getResponseBodyAsStream(), new TypeReference<List<SonarProject>>() {
            });

            if (sonarProjects.isEmpty()) {
                throw new SonarProjectRetrievalException("No sonar project found for repo" + repoName);
            } else if (sonarProjects.size() == 1) {
                log("Found project for repo name {0} - {1}", repoName, sonarProjects.get(0));
                return sonarProjects.get(0);
            } else {
                log("Found multiple projects for repo name {0} - found {1} - returning first result", repoName, sonarProjects.toArray());
                return sonarProjects.get(0);
            }
        } catch (Exception e) {
            throw new SonarProjectRetrievalException(String.format("failed to search for sonar project %s - %s", searchUri, e.getMessage()), e);
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
            return Float.valueOf(value) / 100;
        } catch (Exception e) {
            throw new SonarCoverageMeasureRetrievalException(String.format("failed to get coverage measure for sonar project %s - %s", project.getKey(), e.getMessage()), e);
        }
    }

    private GetMethod executeGetRequest(String uri) throws IOException, HttpClientException {
        final GetMethod method = new GetMethod(uri);
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
