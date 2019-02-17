package com.bullach.android.popularmovies.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bullach.android.popularmovies.MovieRepository;
import com.bullach.android.popularmovies.models.Movie;

import java.util.List;

public class MovieViewModel extends AndroidViewModel {

    private static final String TAG = MovieViewModel.class.getSimpleName();

    private MovieRepository mRepository;
    private LiveData<List<Movie>> mAllMovies;

    public MovieViewModel (@NonNull Application application) {
        super(application);

        mRepository = new MovieRepository(application);
        mAllMovies = mRepository.getAllMovies();
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
    }

    public LiveData<List<Movie>> getAllMovies() { return mAllMovies; }

    public void insert(Movie movie) { mRepository.insert(movie); }
}
