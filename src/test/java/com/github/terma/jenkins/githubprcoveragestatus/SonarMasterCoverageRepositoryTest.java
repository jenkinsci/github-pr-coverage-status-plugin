package com.github.terma.jenkins.githubprcoveragestatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.ClassRule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class SonarMasterCoverageRepositoryTest {

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(0));

    @Test
    public void should_get_coverage() {
        final SonarMasterCoverageRepository sonarMasterCoverageRepository = new SonarMasterCoverageRepository("http://localhost:" + wireMockRule.port());

        givenProjectResponseWithSingleMatch();
        givenMeasureResponse();

        assertThat(sonarMasterCoverageRepository.get("git@github.com:some/my-project.git"), is(0.953f));
    }

    @Test
    public void should_get_zero_coverage_for_not_found() {
        final SonarMasterCoverageRepository sonarMasterCoverageRepository = new SonarMasterCoverageRepository("http://localhost:" + wireMockRule.port());

        givenProjectResponseWithoutMatch();

        assertThat(sonarMasterCoverageRepository.get("git@github.com:some/my-project.git"), is(0f));
    }

    @Test
    public void should_get_zero_coverage_for_unknown_metric() {
        final SonarMasterCoverageRepository sonarMasterCoverageRepository = new SonarMasterCoverageRepository("http://localhost:" + wireMockRule.port());

        givenProjectResponseWithSingleMatch();
        givenNotFoundMeasureResponse();

        assertThat(sonarMasterCoverageRepository.get("git@github.com:some/my-project.git"), is(0f));
    }

    private void givenProjectResponseWithSingleMatch() {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/projects/index"))
                .withQueryParam("search", equalTo("my-project"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("[\n" +
                                "    {\n" +
                                "        \"id\": \"7408\", \n" +
                                "        \"k\": \"my-project:origin/master\", \n" +
                                "        \"nm\": \"my-project origin/master\", \n" +
                                "        \"qu\": \"TRK\", \n" +
                                "        \"sc\": \"PRJ\"\n" +
                                "    }\n" +
                                "]")
                )
        );
    }

    private void givenProjectResponseWithoutMatch() {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/projects/index"))
                .withQueryParam("search", equalTo("my-project"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("[]")
                )
        );
    }

    private void givenMeasureResponse() {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo("overall_line_coverage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\n" +
                                "    \"component\": {\n" +
                                "        \"id\": \"AVYsqpbS80EniRC8Nh21\", \n" +
                                "        \"key\": \"my-project:origin/master\", \n" +
                                "        \"measures\": [\n" +
                                "            {\n" +
                                "                \"metric\": \"overall_line_coverage\", \n" +
                                "                \"periods\": [\n" +
                                "                    {\n" +
                                "                        \"index\": 1, \n" +
                                "                        \"value\": \"0.0\"\n" +
                                "                    }, \n" +
                                "                    {\n" +
                                "                        \"index\": 2, \n" +
                                "                        \"value\": \"0.0\"\n" +
                                "                    }, \n" +
                                "                    {\n" +
                                "                        \"index\": 3, \n" +
                                "                        \"value\": \"0.29999999999999716\"\n" +
                                "                    }\n" +
                                "                ], \n" +
                                "                \"value\": \"95.3\"\n" +
                                "            }\n" +
                                "        ], \n" +
                                "        \"name\": \"my-project origin/master\", \n" +
                                "        \"qualifier\": \"TRK\"\n" +
                                "    }\n" +
                                "}")
                )
        );
    }

    private void givenNotFoundMeasureResponse() {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo("overall_line_coverage"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("{\n" +
                                "    \"errors\": [\n" +
                                "        {\n" +
                                "            \"msg\": \"The following metric keys are not found: overall_line_coverages\"\n" +
                                "        }\n" +
                                "    ]\n" +
                                "}")
                )
        );
    }
}