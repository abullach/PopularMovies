package com.bullach.android.popularmovies;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.bullach.android.popularmovies.database.MovieRoomDatabase;
import com.bullach.android.popularmovies.models.Movie;
import com.bullach.android.popularmovies.database.dao.MovieDao;

import java.util.List;

public class MovieRepository {
    private MovieDao mMovieDao;
    private LiveData<List<Movie>> mAllMovies;

    public MovieRepository(Application application) {
        MovieRoomDatabase db = MovieRoomDatabase.getDatabase(application);
        mMovieDao = db.movieDao();
        mAllMovies = mMovieDao.getAllMovies();
    }

    public LiveData<List<Movie>> getAllMovies() {
        return mAllMovies;
    }


    public void insert (Movie movie) {
        new insertAsyncTask(mMovieDao).execute(movie);
    }

    private static class insertAsyncTask extends AsyncTask<Movie, Void, Void> {

        private MovieDao mAsyncTaskDao;

        insertAsyncTask(MovieDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Movie... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

}
