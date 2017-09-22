package com.github.terma.jenkins.githubprcoveragestatus;

public class Repository {
    private final int id;
    private final String name;

    public Repository(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
