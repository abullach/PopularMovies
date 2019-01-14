package com.bullach.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bullach.android.popularmovies.model.Movie;
import com.bullach.android.popularmovies.utils.QueryUtils;
import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    private static final String TAG = MovieDetailActivity.class.getSimpleName();

    private TextView mMovieTitle;
    private ImageView mMoviePoster;
    private TextView mMovieOverview;
    private TextView mMovieVoteAverage;
    private TextView mMovieVoteAverageLabel;
    private TextView mMovieReleaseDate;
    private TextView mMovieReleaseDateLabel;
    private Movie mMovie;
    private static final int DEFAULT_MARGIN = 8;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mMovieTitle = (TextView) findViewById(R.id.tvMovieTitle);
        mMoviePoster = (ImageView) findViewById(R.id.ivMoviePoster);
        mMovieReleaseDate = (TextView) findViewById(R.id.tvMovieReleaseDate);
        mMovieReleaseDateLabel = (TextView) findViewById(R.id.tvLabelReleaseDate);
        mMovieVoteAverage = (TextView) findViewById(R.id.tvMovieVoteAverage);
        mMovieVoteAverageLabel = (TextView) findViewById(R.id.tvLabelVoteAverage);
        mMovieOverview = (TextView) findViewById(R.id.tvMovieOverview);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        mMovie = getIntent().getParcelableExtra("movieObject");
        if(mMovie != null) {
            populateUI();
        }
    }

    /**
     * Dynamically populate the UI on the MovieDetailActivity depending on available movie data.
     */
    private void populateUI() {
        ConstraintSet set = new ConstraintSet();
        ConstraintLayout layout;
        layout = findViewById(R.id.constraintlayout);

        if(mMovie.getMovieTitle() != null) {
            // Set action bar title
            setTitle(mMovie.getMovieTitle());
            mMovieTitle.setVisibility(View.VISIBLE);
            mMovieTitle.setText(mMovie.getMovieTitle());
        }

        if(mMovie.getMovieReleaseDate() != null) {
            mMovieReleaseDateLabel.setVisibility(View.VISIBLE);
            mMovieReleaseDate.setVisibility(View.VISIBLE);
            mMovieReleaseDate.setText(mMovie.getMovieReleaseDate());
        } else {
            set.clone(layout);
            // Break the connection
            set.clear(R.id.tvLabelReleaseDate, ConstraintSet.TOP);
            set.clear(R.id.tvMovieReleaseDate, ConstraintSet.TOP);
            if(mMovie.getMovieVoteAverage() != null) {
                // Make a new connection
                set.connect(R.id.tvLabelReleaseDate, ConstraintSet.TOP, R.id.ivMoviePoster, ConstraintSet.BOTTOM, DEFAULT_MARGIN);
                set.connect(R.id.tvMovieReleaseDate, ConstraintSet.TOP, R.id.ivMoviePoster, ConstraintSet.BOTTOM, DEFAULT_MARGIN);
            }
            set.applyTo(layout);
        }


        if(mMovie.getMovieVoteAverage() != null) {
            mMovieVoteAverageLabel.setVisibility(View.VISIBLE);
            mMovieVoteAverage.setVisibility(View.VISIBLE);
            mMovieVoteAverage.setText(mMovie.getMovieVoteAverage());
        } else {
            set.clone(layout);
            // Break the connection
            set.clear(R.id.tvMovieOverview, ConstraintSet.TOP);
            if(mMovie.getMovieVoteAverage() != null) {
                // Make a new connection
                set.connect(R.id.tvMovieOverview, ConstraintSet.TOP, R.id.tvLabelReleaseDate, ConstraintSet.BOTTOM, DEFAULT_MARGIN);
            } else {
                // Make a new connection
                set.connect(R.id.tvMovieOverview, ConstraintSet.TOP, R.id.ivMoviePoster, ConstraintSet.BOTTOM, DEFAULT_MARGIN);
            }
            set.applyTo(layout);
        }

        if(mMovie.getMovieOverview() != null) {
            mMovieVoteAverageLabel.setVisibility(View.VISIBLE);
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
