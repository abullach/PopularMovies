package com.bullach.android.popularmovies;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.bullach.android.popularmovies.models.Movie;
import com.bullach.android.popularmovies.utils.QueryUtils;

import java.util.List;

/**
 * Loads a list of movies by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class MovieLoader extends AsyncTaskLoader<List<Movie>> {

    /**
     * Query URL.
     */
    private String mUrl;

    /**
     * Constructs a new {@link MovieLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     */
    public MovieLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Movie> loadInBackground() {
        if(mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response and extract a list of movies.
        return QueryUtils.fetchMovieData(mUrl);
    }
}
