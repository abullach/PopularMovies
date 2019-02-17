package com.bullach.android.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "movie_table")
public class Movie implements Parcelable {

    /**
     * Unique id of the movie.
     */
    @PrimaryKey
    private int movieId;

    /**
     * Original title of the movie.
     */
    @ColumnInfo(name = "title")
    private String movieTitle;

    /**
     * Poster image thumbnail of the movie.
     */
    @ColumnInfo(name = "poster_path")
    private String moviePosterPath;

    /**
     * Plot synopsis of the movie.
     */
    @ColumnInfo(name = "overview")
    private String movieOverview;

    /**
     * User rating (vote average) of the movie.
     */
    @ColumnInfo(name = "vote_average")
    private String movieVoteAverage;

    /**
     * Release date of the movie.
     */
    @ColumnInfo(name = "release_date")
    private String movieReleaseDate;


    public Movie(int movieId, String movieTitle, String moviePosterPath, String movieOverview, String movieVoteAverage, String movieReleaseDate) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.moviePosterPath = moviePosterPath;
        this.movieOverview = movieOverview;
        this.movieVoteAverage = movieVoteAverage;
        this.movieReleaseDate = movieReleaseDate;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getMoviePosterPath() {
        return moviePosterPath;
    }

    public String getMovieOverview() {
        return movieOverview;
    }

    public String getMovieVoteAverage() {
        return movieVoteAverage;
    }

    public String getMovieReleaseDate() {
        return movieReleaseDate;
    }

    @Ignore
    private Movie(Parcel in) {
        movieId = in.readInt();
        movieTitle = in.readString();
        moviePosterPath = in.readString();
        movieOverview = in.readString();
        movieVoteAverage = in.readString();
        movieReleaseDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(movieId);
        dest.writeString(movieTitle);
        dest.writeString(moviePosterPath);
        dest.writeString(movieOverview);
        dest.writeString(movieVoteAverage);
        dest.writeString(movieReleaseDate);
    }

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
