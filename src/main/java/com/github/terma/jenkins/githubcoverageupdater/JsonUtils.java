package com.github.terma.jenkins.githubcoverageupdater;

import com.jayway.jsonpath.JsonPath;

/**
 * Created by stevegal on 24/02/2017.
 */
public class JsonUtils {
    public static <T> T findInJson(String json, String jsonPath) {
        return JsonPath.read(json,jsonPath);
    }
}
