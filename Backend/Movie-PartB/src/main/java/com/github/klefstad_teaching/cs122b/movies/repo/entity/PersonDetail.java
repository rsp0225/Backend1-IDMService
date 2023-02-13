package com.github.klefstad_teaching.cs122b.movies.repo.entity;

public class PersonDetail {
    private Long id;
    private String name;
    private String birthday;
    private String biography;
    private String birthplace;
    private Float popularity;
    private String profilePath;

    public Long getId() {
        return id;
    }

    public PersonDetail setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PersonDetail setName(String name) {
        this.name = name;
        return this;
    }

    public String getBirthday() {
        return birthday;
    }

    public PersonDetail setBirthday(String birthday) {
        this.birthday = birthday;
        return this;
    }

    public String getBiography() {
        return biography;
    }

    public PersonDetail setBiography(String biography) {
        if (biography != null) {
            this.biography = biography.replace("\r","");
            return this;
        }
        return this;
    }

    public String getBirthplace() {
        return birthplace;
    }

    public PersonDetail setBirthplace(String birthplace) {
        this.birthplace = birthplace;
        return this;
    }

    public Float getPopularity() {
        return popularity;
    }

    public PersonDetail setPopularity(Float popularity) {
        this.popularity = popularity;
        return this;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public PersonDetail setProfilePath(String profilePath) {
        this.profilePath = profilePath;
        return this;
    }
}
