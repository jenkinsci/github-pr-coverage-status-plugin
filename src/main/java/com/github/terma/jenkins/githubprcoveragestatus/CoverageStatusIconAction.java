package com.github.terma.jenkins.githubprcoveragestatus;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

@Extension
public class CoverageStatusIconAction implements UnprotectedRootAction {

    public static final String URL_NAME = "stash-coverage-status-icon";

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }

    /**
     * Used by Jenkins Stapler service when get request on URL jenkins_host/getUrlName()
     *
     * @param request - request
     * @param response - response
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public void doIndex(StaplerRequest request, StaplerResponse response) throws IOException {
        final float coverage = Float.parseFloat(request.getParameter("coverage"));
        final float masterCoverage = Float.parseFloat(request.getParameter("masterCoverage"));
        final String color = toHexColor(request.getParameter("color"));

        response.setContentType("image/svg+xml");

        String svg = IOUtils.toString(this.getClass().getResourceAsStream(
                "/com/github/terma/jenkins/githubprcoveragestatus/Icon/icon.svg"));

        final Message message = new Message(coverage, masterCoverage);
        svg = StringUtils.replace(svg, "{{ message }}", message.forIcon());

        final int coveragePercent = Percent.of(coverage);
        svg = StringUtils.replace(svg, "{{ color }}", color);

        response.getWriter().write(svg);
    }

    private String toHexColor(String color) {
        if (StringUtils.startsWith(color, "#")) {
            return color;
        }

        if (StringUtils.equalsIgnoreCase("red", color)) {
            return "#b94947"; // red
        } else if (StringUtils.equalsIgnoreCase("yellow", color)) {
            return "#F89406"; // yellow
        } else if (StringUtils.equalsIgnoreCase("brightgreen", color)) {
            return "#97CA00"; // brightgreen
        } else if (StringUtils.equalsIgnoreCase("green", color)) {
            return "#008000"; // green
        }
        return color;
    }

}
