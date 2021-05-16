package com.example.moviesapp;

public class MovieModelClass {
    private String id;
    private String name;
    private String img;
    private String description;

    public MovieModelClass(String id, String name, String img, String description) {
        this.id = id;
        this.name = name;
        this.img = img;
        this.description = description;
    }

    public MovieModelClass() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
