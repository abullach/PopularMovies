package com.bullach.android.popularmovies.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.bullach.android.popularmovies.models.Movie;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie_table ORDER BY vote_average DESC")
    LiveData<List<Movie>> getAllMovies();

    @Insert
    void insert(Movie movie);

    @Delete
    void delete(Movie movie);

    @Query("SELECT * FROM movie_table WHERE movieId = :id")
    Movie getMovie(String id);
}
