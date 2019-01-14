package com.bullach.android.popularmovies;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bullach.android.popularmovies.model.Movie;
import com.bullach.android.popularmovies.utils.QueryUtils;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;

public class MainActivity extends AppCompatActivity
        implements LoaderCallbacks<List<Movie>>, MoviesAdapter.MoviesAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener  {

    /**
     * Constant value for the movie loader ID.
     */
    private static final int MOVIE_LOADER_ID = 1;

    /**
     * Adapter for the list of movies
     */
    private MoviesAdapter mAdapter;

    private RecyclerView mRecyclerView;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.rvMovies);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        // Show empty state text view if there is no data or while data loads
        mEmptyStateTextView = findViewById(R.id.empty_view);
        mEmptyStateTextView.setVisibility(View.VISIBLE);

        mAdapter = new MoviesAdapter(MainActivity.this, new ArrayList<Movie>(), this);
        mRecyclerView.setAdapter(mAdapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // And register to be notified of preference changes
        // So we know when the user has adjusted the query settings
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(MOVIE_LOADER_ID, null, this);
        } else {
            // First, hide loading indicator so error message will be visible
            hideLoadingIndicator();

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    private void hideLoadingIndicator() {
        // First, hide loading indicator so error message will be visible
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void showLoadingIndicator() {
        // First, hide loading indicator so error message will be visible
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showMovieData() {
        mEmptyStateTextView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(INVISIBLE);
        // Set empty state text to display "No movies found. Please refresh the app or try again later."
        mEmptyStateTextView.setText(R.string.no_movies_found);
        mEmptyStateTextView.setVisibility(View.VISIBLE);
    }

    /**
     * This method is overridden by the MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param movie The movie that was clicked
     */
    @Override
    public void onClick(Movie movie) {
        Intent i = new Intent(MainActivity.this, MovieDetailActivity.class);
        i.putExtra("movieObject", movie); // using the (String name, Parcelable value) overload!
        startActivity(i); // dataToSend is now passed to the new Activity
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAdapter.clear();

        // Hide the empty state text view as the loading indicator will be displayed
        mEmptyStateTextView.setVisibility(View.GONE);

        // Show the loading indicator while new data is being fetched
        showLoadingIndicator();

        // Restart the loader to re-query the Movies API as the query settings have been updated
        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this).forceLoad();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.settings_order_by_key))) {
            // Hide the empty state text view as the loading indicator will be displayed
            mEmptyStateTextView.setVisibility(View.GONE);
            // Show the loading indicator while new data is being fetched
            showLoadingIndicator();
            // Restart the loader to re-query the Movies API as the query settings have been updated
            getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this).forceLoad();
        }
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle bundle) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

            String path = QueryUtils.POPULAR_MOVIE_PATH;

            String orderBy = sharedPrefs.getString(
                    getString(R.string.settings_order_by_key),
                    getString(R.string.settings_order_by_default)
            );

            if (orderBy != null && !orderBy.equals(getString(R.string.settings_order_by_default))) {
                path = QueryUtils.TOP_RATED_MOVIE_PATH;
            }

            return new MovieLoader(this, QueryUtils.buildUrl(this, path).toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        mAdapter.clear();

        // If there is a valid list of {@link Movies}s, then add them to the adapter's
        // data set. This will trigger the RecyclerView to update.
        if (movies != null && !movies.isEmpty()) {
            showMovieData();
            mAdapter.addAll(movies);
            mRecyclerView.setHasFixedSize(true);
        } else {
            // Set empty state text to display "No movies found. Please refresh the app or try again later."
            showErrorMessage();
        }
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
