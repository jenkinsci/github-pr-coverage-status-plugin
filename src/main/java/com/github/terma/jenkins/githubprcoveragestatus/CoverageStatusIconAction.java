package com.github.terma.jenkins.githubprcoveragestatus;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Extension
public class CoverageStatusIconAction implements UnprotectedRootAction {

    public static final String URL_NAME = "stash-coverage-status-icon";
    private final int baseIconWidth = 70;
    private final int baseFontXPos = 65;

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
        final String branch = StringUtils.defaultIfBlank(request.getParameter("branch"), "master");
        final String color = toHexColor(request.getParameter("color"));

        response.setContentType("image/svg+xml");

        String svg = IOUtils.toString(this.getClass().getResourceAsStream(
                "/com/github/terma/jenkins/githubprcoveragestatus/Icon/icon.svg"));

        final Message message = new Message(coverage, masterCoverage, branch);
        final String iconMessage = message.forIcon();
        svg = StringUtils.replace(svg, "{{ message }}", iconMessage);

        svg = StringUtils.replace(svg, "{{ color }}", color);
        final int fontPixel = getFontPixel(iconMessage);
        svg = StringUtils.replace(svg, "{{ totalwidth }}", String.valueOf(baseIconWidth + fontPixel));
        svg = StringUtils.replace(svg, "{{ fontXPos }}", String.valueOf(baseFontXPos + fontPixel / 2));

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

    private int getFontPixel(String text) {
        Font defaultFont = new Font("DejaVu Sans", Font.PLAIN, 11);
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        FontMetrics fm = g2d.getFontMetrics(defaultFont);
        final int textSize = fm.stringWidth(text);
        g2d.dispose();
        return textSize;
    }

}
