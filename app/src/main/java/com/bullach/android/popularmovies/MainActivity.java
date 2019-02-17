package com.bullach.android.popularmovies;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bullach.android.popularmovies.adapters.MoviesAdapter;
import com.bullach.android.popularmovies.models.Movie;
import com.bullach.android.popularmovies.utils.QueryUtils;
import com.bullach.android.popularmovies.viewmodel.MovieViewModel;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static com.bullach.android.popularmovies.utils.Constants.SELECTED_MOVIE;

public class MainActivity extends AppCompatActivity
        implements LoaderCallbacks<List<Movie>>, MoviesAdapter.MoviesAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener  {

    private static final int MOVIE_LOADER_ID = 1;
    private MovieViewModel mMovieViewModel;
    private MoviesAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TextView mEmptyStateTextView;
    private static boolean SHARED_PREF_CHANGED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.rvMovies);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        }

        showEmptyState();

        mAdapter = new MoviesAdapter(MainActivity.this, new ArrayList<Movie>(), this);
        mRecyclerView.setAdapter(mAdapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            setupViewModel();
        } else {
            hideLoadingIndicator();
            mEmptyStateTextView.setText(R.string.no_network_message);
        }
    }

    private void setupViewModel() {
        mMovieViewModel = ViewModelProviders.of(this).get(MovieViewModel.class);
        mMovieViewModel.getAllMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                if(isSortOrderFavorite()) {
                    if(movies!=null && !movies.isEmpty()) {
                        showMovieData();
                        mAdapter.setMoviesData(movies);
                    } else
                        showNoFavoritesMessage();
                }
                else {
                    LoaderManager loaderManager = getLoaderManager();
                    loaderManager.initLoader(MOVIE_LOADER_ID, null, MainActivity.this);
                }
            }
        });
    }

    private void showEmptyState() {
        mEmptyStateTextView = findViewById(R.id.empty_view);
        mEmptyStateTextView.setVisibility(View.VISIBLE);
    }

    private void showLoadingIndicator() {
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void showMovieData() {
        mEmptyStateTextView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(INVISIBLE);
        mEmptyStateTextView.setText(R.string.no_movies_found);
        mEmptyStateTextView.setVisibility(View.VISIBLE);
    }

    private void showNoFavoritesMessage() {
        mRecyclerView.setVisibility(INVISIBLE);
        mEmptyStateTextView.setText(R.string.no_favorites_found);
        mEmptyStateTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Movie movie) {
        Intent i = new Intent(MainActivity.this, MovieDetailActivity.class);
        i.putExtra(SELECTED_MOVIE, movie);
        startActivity(i);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle bundle) {
        String path = QueryUtils.SORT_TYPE_POPULAR;

        String sortOrder = getPreferredSortOrder(this);
        if (sortOrder != null && !sortOrder.equals(getString(R.string.settings_order_by_default))) {
            path = QueryUtils.SORT_TYPE_TOP_RATED;
        }
        return new MovieLoader(this, QueryUtils.buildMovieUrl(path).toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        hideLoadingIndicator();

        mAdapter.clear();
        if (movies != null && !movies.isEmpty()) {
            showMovieData();
            hideLoadingIndicator();
            mAdapter.setMoviesData(movies);
            mRecyclerView.setHasFixedSize(true);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        SHARED_PREF_CHANGED = true;
    }

    public String getPreferredSortOrder(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));
    }

    protected boolean isSortOrderFavorite() {
        String preferredSortOrder = getPreferredSortOrder(MainActivity.this);
        String sortOrderFavorite = getString(R.string.settings_order_by_favorites_value);
        return preferredSortOrder.equals(sortOrderFavorite);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(SHARED_PREF_CHANGED) {
            if (isSortOrderFavorite()) {
                List<Movie> movies = mMovieViewModel.getAllMovies().getValue();
                if (movies != null && !movies.isEmpty()) {
                    hideLoadingIndicator();
                    mAdapter.setMoviesData(movies);
                } else
                    showNoFavoritesMessage();
            } else {
                mEmptyStateTextView.setVisibility(View.GONE);
                showLoadingIndicator();
                getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this).forceLoad();
            }
            SHARED_PREF_CHANGED = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
