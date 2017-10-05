package com.github.terma.jenkins.githubprcoveragestatus;

public class PullRequest {
    private final String id;
    private final String name;

    public PullRequest(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
