package com.bullach.android.popularmovies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bullach.android.popularmovies.adapters.ReviewsAdapter;
import com.bullach.android.popularmovies.adapters.TrailersAdapter;
import com.bullach.android.popularmovies.database.MovieRoomDatabase;
import com.bullach.android.popularmovies.models.Movie;
import com.bullach.android.popularmovies.models.Review;
import com.bullach.android.popularmovies.models.Trailer;
import com.bullach.android.popularmovies.utils.QueryUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

import static com.bullach.android.popularmovies.utils.Constants.SELECTED_MOVIE;
import static com.bullach.android.popularmovies.utils.Constants.YOUTUBE_BASE_URI;
import static com.bullach.android.popularmovies.utils.MovieJSONUtils.extractReviewsFromJsonResponse;
import static com.bullach.android.popularmovies.utils.MovieJSONUtils.extractTrailersFromJsonResponse;

public class MovieDetailActivity extends AppCompatActivity implements TrailersAdapter.TrailersOnClickListener,
        LoaderManager.LoaderCallbacks {

    private final static String TAG = MovieDetailActivity.class.getSimpleName();

    private TextView mMovieTitle;
    private ImageView mMoviePoster;
    private Button mMovieFavoriteButton;
    private TextView mMovieOverview;
    private TextView mMovieVoteAverage;
    private TextView mMovieReleaseDate;
    private TextView mNoTrailersMessage;
    private TextView mNoReviewsMessage;
    private TextView mNoNetworkMessage;
    private ProgressBar mTrailersLoadingIndicator;
    private ProgressBar mReviewsLoadingIndicator;
    private static String mId;
    private static final int DEFAULT_MARGIN = 8;
    private Movie mMovie;
    private RecyclerView mTrailersRecyclerView;
    private RecyclerView mReviewsRecyclerView;
    private TrailersAdapter mTrailersAdapter;
    private ReviewsAdapter mReviewsAdapter;
    private MovieRoomDatabase mMovieDb;
    private static boolean FAVORITE_MOVIE;
    private static final int TRAILER_LOADER_ID = 1;
    private static final int REVIEW_LOADER_ID = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bindViews();
        mMovieDb = MovieRoomDatabase.getDatabase(getApplicationContext());

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        mMovie = getIntent().getParcelableExtra(SELECTED_MOVIE);
        if(mMovie != null) {
            populateUI();

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                mId = String.valueOf(mMovie.getMovieId());
                setUpTrailers();
                setUpReviews();
            } else {
                showNoNetworkMessage();
            }
        }
    }

    private void bindViews() {
        mMovieTitle = findViewById(R.id.tvMovieTitle);
        mMoviePoster = findViewById(R.id.ivMoviePoster);
        mMovieFavoriteButton = findViewById(R.id.btFavorite);
        mMovieReleaseDate = findViewById(R.id.tvMovieReleaseDate);
        mMovieVoteAverage = findViewById(R.id.tvMovieVoteAverage);
        mMovieOverview = findViewById(R.id.tvMovieOverview);
        mTrailersRecyclerView = findViewById(R.id.rvTrailers);
        mReviewsRecyclerView = findViewById(R.id.rvReviews);
        mNoTrailersMessage = findViewById(R.id.tvNoTrailersMessage);
        mNoReviewsMessage = findViewById(R.id.tvNoReviewMessage);
        mNoNetworkMessage = findViewById(R.id.tvNoNetworkMessage);
        mTrailersLoadingIndicator = findViewById(R.id.pbloadingIndicatorTrailer);
        mReviewsLoadingIndicator = findViewById(R.id.pbloadingIndicatorReviews);
    }

    private void populateUI() {
        ConstraintSet set = new ConstraintSet();
        ConstraintLayout layout;
        layout = findViewById(R.id.constraintlayout);

        if(mMovie.getMovieTitle() != null) {
            setTitle(getString(R.string.toolbar_title_detail));
            mMovieTitle.setVisibility(View.VISIBLE);
            mMovieTitle.setText(mMovie.getMovieTitle());
        }

        if(mMovie.getMovieReleaseDate() != null) {
            mMovieReleaseDate.setVisibility(View.VISIBLE);
            mMovieReleaseDate.setText(mMovie.getMovieReleaseDate());
        } else {
            set.clone(layout);
            set.clear(R.id.tvMovieReleaseDate, ConstraintSet.TOP);
            if(mMovie.getMovieVoteAverage() != null) {
                set.connect(R.id.tvMovieReleaseDate, ConstraintSet.TOP, R.id.ivMoviePoster, ConstraintSet.BOTTOM, DEFAULT_MARGIN);
            }
            set.applyTo(layout);
        }

        if(mMovie.getMovieVoteAverage() != null) {
            mMovieVoteAverage.setVisibility(View.VISIBLE);
            mMovieVoteAverage.setText(mMovie.getMovieVoteAverage());
        }

        if(mMovie.getMovieOverview() != null) {
            mMovieOverview.setVisibility(View.VISIBLE);
            mMovieOverview.setText(mMovie.getMovieOverview());
        }

        if(mMovie.getMoviePosterPath() != null) {
            Uri uri = QueryUtils.buildPosterImageUrl(mMovie.getMoviePosterPath(), QueryUtils.POSTER_IMAGE_SIZE_W500_PATH);
            Picasso.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_poster_placeholder_original_black_24dp)
                    .error(R.drawable.ic_error_outline_black_24dp)
                    .into(mMoviePoster);
        }

        setupFavoritesButton(String.valueOf(mMovie.getMovieId()));
    }

    private void setupFavoritesButton(final String movieId) {
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                Movie movie = mMovieDb.movieDao().getMovie(movieId);
                if(movie == null) {
                    FAVORITE_MOVIE = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMovieFavoriteButton.setText(R.string.button_favorite_label);
                        }
                    });
                } else {
                    FAVORITE_MOVIE = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMovieFavoriteButton.setText(R.string.button_unfavorite_label);
                        }
                    });
                }
            }
        });

        mMovieFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentOrigin = getIntent();
                final Movie movie = intentOrigin.getParcelableExtra(SELECTED_MOVIE);
                if(FAVORITE_MOVIE)
                    AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mMovieDb.movieDao().delete(movie);
                            FAVORITE_MOVIE = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMovieFavoriteButton.setText(R.string.button_favorite_label);
                                }
                            });
                        }
                    });
                else
                    AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mMovieDb.movieDao().insert(movie);
                            FAVORITE_MOVIE = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMovieFavoriteButton.setText(R.string.button_unfavorite_label);
                                }
                            });
                        }
                    });
            }
        });
    }

    protected void setUpTrailers() {
        LinearLayoutManager mLinearLayoutManagerTrailers;
        mLinearLayoutManagerTrailers = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mTrailersRecyclerView.setLayoutManager(mLinearLayoutManagerTrailers);
        mTrailersRecyclerView.setHasFixedSize(true);

        mTrailersAdapter = new TrailersAdapter(this, this);
        mTrailersRecyclerView.setAdapter(mTrailersAdapter);

        getSupportLoaderManager().initLoader(TRAILER_LOADER_ID, null, this);
    }

    @Override
    public void trailerOnClick(Trailer trailer) {
        Intent intentYoutubeApp = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_BASE_URI + trailer.getKey()));
        if(intentYoutubeApp.resolveActivity(getPackageManager()) != null)
            startActivity(intentYoutubeApp);
    }

    protected void setUpReviews() {
        LinearLayoutManager mLinearLayoutManagerReviews;
        mLinearLayoutManagerReviews = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mReviewsRecyclerView.setLayoutManager(mLinearLayoutManagerReviews);
        mReviewsRecyclerView.setHasFixedSize(true);

        mReviewsAdapter = new ReviewsAdapter(this);
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);

        getSupportLoaderManager().initLoader(REVIEW_LOADER_ID, null, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if(id == TRAILER_LOADER_ID)
            return onCreateTrailersLoader();
        else
            return onCreateReviewsLoader();
    }

    @SuppressLint("StaticFieldLeak")
    private Loader<List<Trailer>> onCreateTrailersLoader() {
        return new AsyncTaskLoader<List<Trailer>>(this) {

            List<Trailer> mTrailersData = null;

            @Override
            protected void onStartLoading() {
                if(mTrailersData != null) {
                    deliverResult(mTrailersData);
                } else {
                    mTrailersLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public List<Trailer> loadInBackground() {
                URL trailersRequestUrl = QueryUtils.buildTrailersUrl(mId);
                try {
                    String trailersResponse = QueryUtils.getResponseFromHttpUrl(trailersRequestUrl);
                    return extractTrailersFromJsonResponse(trailersResponse);
                } catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(List<Trailer> data) {
                mTrailersData = data;
                super.deliverResult(data);
            }
        };
    }

    @SuppressLint("StaticFieldLeak")
    private Loader<List<Review>> onCreateReviewsLoader() {
        return new AsyncTaskLoader<List<Review>>(this) {

            List<Review> mReviewsData = null;

            @Override
            protected void onStartLoading() {
                if(mReviewsData != null) {
                    deliverResult(mReviewsData);
                } else {
                    mReviewsLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public List<Review> loadInBackground() {
                URL reviewsRequestUrl = QueryUtils.buildReviewsUrl(mId);

                try {
                    String reviewsResponse = QueryUtils.getResponseFromHttpUrl(reviewsRequestUrl);
                    return extractReviewsFromJsonResponse(reviewsResponse);
                } catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(List<Review> data) {
                mReviewsData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {

        if (loader.getId() == TRAILER_LOADER_ID) {
            if(data != null) {
                List<Trailer> trailersData = (List<Trailer>) data;
                mTrailersLoadingIndicator.setVisibility(View.INVISIBLE);
                if (!trailersData.isEmpty()) {
                    mTrailersRecyclerView.setVisibility(View.VISIBLE);
                    mTrailersAdapter.setTrailersData(trailersData);
                } else
                    showNoTrailersMessage();
                }
            }
        else {
            if(data != null) {
                mReviewsLoadingIndicator.setVisibility(View.INVISIBLE);
                List<Review> reviewsData = (List<Review>) data;
                if (!reviewsData.isEmpty()) {
                    mReviewsRecyclerView.setVisibility(View.VISIBLE);
                    mReviewsAdapter.setReviewsAdapter(reviewsData);
                } else {
                    showNoReviewsMessage();
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    protected void showNoTrailersMessage() {
        mTrailersRecyclerView.setVisibility(View.INVISIBLE);
        mNoNetworkMessage.setVisibility(View.INVISIBLE);
        mNoTrailersMessage.setVisibility(View.VISIBLE);
    }

    protected void showNoReviewsMessage() {
        mReviewsRecyclerView.setVisibility(View.INVISIBLE);
        mNoNetworkMessage.setVisibility(View.INVISIBLE);
        mNoReviewsMessage.setVisibility(View.VISIBLE);
    }

    protected void showNoNetworkMessage() {
        mTrailersRecyclerView.setVisibility(View.INVISIBLE);
        mReviewsRecyclerView.setVisibility(View.INVISIBLE);
        mNoNetworkMessage.setVisibility(View.VISIBLE);
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.no_movies_found, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
