package com.bullach.android.popularmovies.models;

public class Trailer {
    private int movieId;
    private String key;
    private String name;

    public Trailer(int movieId, String key, String name){
        this.movieId = movieId;
        this.key = key;
        this.name = name;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }
}
