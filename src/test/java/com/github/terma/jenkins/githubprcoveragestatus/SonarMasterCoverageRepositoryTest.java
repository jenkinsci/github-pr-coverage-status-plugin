package com.github.terma.jenkins.githubprcoveragestatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.google.common.base.Charsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class SonarMasterCoverageRepositoryTest {

    private static final String GIT_REPO_NAME = "my-project";
    private static final String GIT_REPO_URL = "http://test.com/user/" + GIT_REPO_NAME;
    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(0));

    private SonarMasterCoverageRepository sonarMasterCoverageRepository;
    private ByteArrayOutputStream buildLogOutputStream;

    private SettingsRepository settingsRepository = mock(SettingsRepository.class);
    
    @Before
    public void initMocks() throws IOException {
        ServiceRegistry.setSettingsRepository(settingsRepository);
    }
    
    @After
    public void afterTest() throws Exception {
        System.out.println(buildLogOutputStream.toString());
        wireMockRule.resetMappings();
    }

    @Test
    public void should_get_coverage_default() throws IOException {
        testCoverage(null, null, null, 0.953f);
        testCoverage("token", "", null, 0.953f);
        testCoverage("login", "password", null, 0.953f);
    }

    @Test
    public void should_get_coverage_branch() throws IOException {
        testCoverage(null, null, SonarMasterCoverageRepository.SONAR_OVERALL_BRANCH_COVERAGE_METRIC_NAME, 0.753f);
        testCoverage("token", "", SonarMasterCoverageRepository.SONAR_OVERALL_BRANCH_COVERAGE_METRIC_NAME, 0.753f);
        testCoverage("login", "password", SonarMasterCoverageRepository.SONAR_OVERALL_BRANCH_COVERAGE_METRIC_NAME, 0.753f);
    }

    @Test
    public void should_get_coverage_instruction() throws IOException {
        testCoverage(null, null, SonarMasterCoverageRepository.SONAR_OVERALL_INSTRUCTION_COVERAGE_METRIC_NAME, 0.953f);
        testCoverage("token", "", SonarMasterCoverageRepository.SONAR_OVERALL_INSTRUCTION_COVERAGE_METRIC_NAME, 0.953f);
        testCoverage("login", "password", SonarMasterCoverageRepository.SONAR_OVERALL_INSTRUCTION_COVERAGE_METRIC_NAME, 0.953f);
    }

    @Test
    public void should_get_coverage_line() throws IOException {
        testCoverage(null, null, SonarMasterCoverageRepository.SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME, 0.852f);
        testCoverage("token", "", SonarMasterCoverageRepository.SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME, 0.852f);
        testCoverage("login", "password", SonarMasterCoverageRepository.SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME, 0.852f);
    }

    private void testCoverage(final String login, final String password, final String metric, final float expected) throws IOException {
        givenCoverageRepository(login, password);
        givenProjectResponseWithSingleMatch(login, password);

        givenMeasureResponse();

        Mockito.when(settingsRepository.getSonarCoverageMetric()).thenReturn(metric);
        
        final float coverage = sonarMasterCoverageRepository.get(GIT_REPO_URL);
        assertThat(coverage, is(expected));
    }

    @Test
    public void should_get_coverage_for_multiple_projects_found() throws IOException {
        givenCoverageRepository(null, null);

        givenProjectResponseWithMultipleMatches();
        givenMeasureResponse();

        Mockito.when(settingsRepository.getSonarCoverageMetric()).thenReturn(null);

        final float coverage = sonarMasterCoverageRepository.get(GIT_REPO_URL);
        assertThat(coverage, is(0.953f));
    }

    @Test
    public void should_get_zero_coverage_for_not_found() {
        givenCoverageRepository(null, null);

        givenProjectResponseWithoutMatch();

        Mockito.when(settingsRepository.getSonarCoverageMetric()).thenReturn(null);

        assertThat(sonarMasterCoverageRepository.get(GIT_REPO_URL), is(0f));
    }

    @Test
    public void should_get_zero_coverage_for_unknown_metric() throws IOException {
        givenCoverageRepository(null, null);

        givenProjectResponseWithSingleMatch(null, null);
        givenNotFoundMeasureResponse();

        Mockito.when(settingsRepository.getSonarCoverageMetric()).thenReturn("unknown_coverage");

        assertThat(sonarMasterCoverageRepository.get(GIT_REPO_URL), is(0f));
    }

    private void givenCoverageRepository(final String login, String password) {
        buildLogOutputStream = new ByteArrayOutputStream();
        sonarMasterCoverageRepository = new SonarMasterCoverageRepository("http://localhost:" + wireMockRule.port(),
                login, password, new PrintStream(buildLogOutputStream, true));
    }

    private void givenProjectResponseWithSingleMatch(final String login, String password) throws IOException {
        final MappingBuilder search = get(urlPathEqualTo("/api/projects/index"))
                .withQueryParam("search", equalTo(GIT_REPO_NAME));
        if (login != null) {
            search.withBasicAuth(login, password);
        }
        wireMockRule.stubFor(search.willReturn(
                aResponse().withStatus(200)
                        .withBody(getResponseBodyFromFile("singleProjectFound.json"))
                )
        );
    }

    private void givenProjectResponseWithMultipleMatches() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/projects/index"))
                .withQueryParam("search", equalTo(GIT_REPO_NAME))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getResponseBodyFromFile("multipleProjectsFound.json"))
                )
        );
    }

    private void givenProjectResponseWithoutMatch() {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/projects/index"))
                .withQueryParam("search", equalTo(GIT_REPO_NAME))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("[]")
                )
        );
    }

    private void givenMeasureResponse() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo(SonarMasterCoverageRepository.SONAR_OVERALL_INSTRUCTION_COVERAGE_METRIC_NAME))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getResponseBodyFromFile("measureFound.json"))
                )
        );
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo(SonarMasterCoverageRepository.SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getResponseBodyFromFile("measureFoundLine.json"))
                )
        );
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo(SonarMasterCoverageRepository.SONAR_OVERALL_BRANCH_COVERAGE_METRIC_NAME))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getResponseBodyFromFile("measureFoundBranch.json"))
                )
        );
    }

    private void givenNotFoundMeasureResponse() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo(SonarMasterCoverageRepository.SONAR_OVERALL_INSTRUCTION_COVERAGE_METRIC_NAME))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody(getResponseBodyFromFile("metricNotFound.json"))
                )
        );
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo(SonarMasterCoverageRepository.SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody(getResponseBodyFromFile("metricNotFound.json"))
                )
        );
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo(SonarMasterCoverageRepository.SONAR_OVERALL_BRANCH_COVERAGE_METRIC_NAME))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody(getResponseBodyFromFile("metricNotFound.json"))
                )
        );
    }

    private String getResponseBodyFromFile(String fileName) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream("/com/github/terma/jenkins/githubprcoveragestatus/SonarMasterCoverageRepositoryTest/" + fileName), UTF_8);
    }
}