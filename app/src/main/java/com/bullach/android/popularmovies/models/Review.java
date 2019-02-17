package com.bullach.android.popularmovies.models;

public class Review {
    private int movieId;
    private String author;
    private String review;

    public Review(int movieId, String author, String review){
        this.movieId = movieId;
        this.author = author;
        this.review = review;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getAuthor() {
        return author;
    }

    public String getReview() {
        return review;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
