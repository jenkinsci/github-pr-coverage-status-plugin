package com.github.terma.jenkins.githubprcoveragestatus;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CoverageStatusIconActionTest {

    CoverageStatusIconAction action;
    StaplerRequest request;
    StaplerResponse response;
    ByteArrayOutputStream outputStream;
    PrintWriter printWriter;

    @Before
    @SneakyThrows
    public void before() {
        action = new CoverageStatusIconAction();
        outputStream = new ByteArrayOutputStream();
        request = Mockito.mock(StaplerRequest.class);
        response = Mockito.mock(StaplerResponse.class);
        printWriter = new PrintWriter(outputStream);
        when(request.getParameter("coverage")).thenReturn("0.56");
        when(request.getParameter("masterCoverage")).thenReturn("0.51");
        when(request.getParameter("color")).thenReturn("red");
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    public void testGetter() {
        assertThat("DisplayName", action.getDisplayName(), nullValue());
        assertThat("IconFileName", action.getIconFileName(), nullValue());
        assertThat("UrlName", action.getUrlName(), equalTo(CoverageStatusIconAction.URL_NAME));
    }

    @Test
    public void verifyIconIsReturnedInRed() {
        when(request.getParameter("color")).thenReturn("red");
        verifyIconIsReturned("#b94947");
    }

    @Test
    public void verifyIconIsReturnedInGreen() {
        when(request.getParameter("color")).thenReturn("brightgreen");
        verifyIconIsReturned("#97CA00");
    }

    @Test
    public void verifyIconIsReturnedInYellow() {
        when(request.getParameter("color")).thenReturn("yellow");
        verifyIconIsReturned("#F89406");
    }

    @Test
    public void verifyIconIsReturnedWithHexColor() {
        when(request.getParameter("color")).thenReturn("#997755");
        verifyIconIsReturned("#997755");
    }

    @SneakyThrows
    private void verifyIconIsReturned(String expectedColor) {
        action.doIndex(request, response);
        printWriter.flush();
        assertThat("Icon body", outputStream.toString(), equalTo(getExpectedBody("56% (+5.0%) vs master 51%", expectedColor)));
        verify(response, times(1)).setContentType("image/svg+xml");
    }

    @SneakyThrows
    private String getExpectedBody(String message, String color) {
        String svg = IOUtils.toString(this.getClass().getResourceAsStream(
            "/com/github/terma/jenkins/githubprcoveragestatus/Icon/icon.svg"));
        return svg.replace("{{ message }}", message).replace("{{ color }}", color);
    }

}