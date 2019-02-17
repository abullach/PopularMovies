package com.bullach.android.popularmovies.utils;

import android.text.TextUtils;
import android.util.Log;

import com.bullach.android.popularmovies.models.Movie;
import com.bullach.android.popularmovies.models.Review;
import com.bullach.android.popularmovies.models.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MovieJSONUtils {

    private final static String TAG = MovieJSONUtils.class.getSimpleName();

    private static final String STATUS_CODE = "status_code";
    private static final String STATUS_MESSAGE = "status_message";
    private static final String JSON_PARSE_ERROR = "Problem parsing the movies JSON results.";

    private static final String RESULTS = "results";
    private static final String MOVIE_ID = "id";

    private static final String MOVIE_TITLE = "title";
    private static final String MOVIE_POSTER_PATH = "poster_path";
    private static final String MOVIE_SYNOPSIS = "overview";
    private static final String MOVIE_VOTE_AVERAGE = "vote_average";
    private static final String MOVIE_RELEASE_DATE = "release_date";

    private static final String MOVIE_TRAILER_KEY = "key";
    private static final String MOVIE_TRAILER_NAME = "name";

    private static final String MOVIE_REVIEW_AUTHOR = "author";
    private static final String MOVIE_REVIEW_CONTENT = "content";

    /**
     * Parses JSON response from the "themoviedb.org" movies API and returns a list of {@link Movie} objects.
     * <br>
     * @param movieJSONResponse JSON response from server.
     * @return List of {@link Movie} objects.
     */
    public static List<Movie> extractMoviesFromJson(String movieJSONResponse) {
        if (TextUtils.isEmpty(movieJSONResponse)) {
            return null;
        }
        List<Movie> movieList = new ArrayList<>();

        try {
            JSONObject movieJSONObject = new JSONObject(movieJSONResponse);

            // https://www.themoviedb.org/documentation/api/status-codes?language=en-US
            if(movieJSONObject.has(STATUS_CODE)) {
                int statusCode = movieJSONObject.getInt(STATUS_CODE);
                String statusMessage = movieJSONObject.optString(STATUS_MESSAGE);
                Log.d(TAG, "Status code: " +statusCode +"Status message: " +statusMessage);

                switch(statusCode) {
                    case 1: // HTTP Status 200
                        break;
                    case 3: // HTTP Status 401 (Invalid API key)
                        return null;
                    case 6: // HTTP Status 404 (Invalid id)
                        return null;
                    default:
                        return null;
                }
            }

            JSONArray movieResultsArray = movieJSONObject.getJSONArray(RESULTS);

            for (int i = 0; i < movieResultsArray.length(); i++) {
                JSONObject results = movieResultsArray.getJSONObject(i);
                int id = results.optInt(MOVIE_ID);
                String title = results.optString(MOVIE_TITLE);
                String poster_path = results.optString(MOVIE_POSTER_PATH);
                String overview = results.optString(MOVIE_SYNOPSIS);
                String vote_average = results.optString(MOVIE_VOTE_AVERAGE);
                String release_date = results.optString(MOVIE_RELEASE_DATE);
                Movie movies = new Movie(id, title, poster_path, overview, vote_average, release_date);
                movieList.add(movies);
            }
        } catch (JSONException e) {
            Log.e(TAG, JSON_PARSE_ERROR, e);
        }
        return movieList;
    }


    /**
     * Parses JSON response from the "themoviedb.org" videos API and returns a list of {@link Trailer} objects.
     * <br>
     * @param trailerJSONResponse JSON response from server.
     * @return List of {@link Trailer} objects.
     */
    public static List<Trailer> extractTrailersFromJsonResponse(String trailerJSONResponse) {
        if (TextUtils.isEmpty(trailerJSONResponse)) {
            return null;
        }
        List<Trailer> trailerList = new ArrayList<>();

        try {
            JSONObject trailerJSONObject = new JSONObject(trailerJSONResponse);

            // https://www.themoviedb.org/documentation/api/status-codes?language=en-US
            if(trailerJSONObject.has(STATUS_CODE)) {
                int statusCode = trailerJSONObject.getInt(STATUS_CODE);
                String statusMessage = trailerJSONObject.optString(STATUS_MESSAGE);
                Log.d(TAG, "Status code: " +statusCode +"Status message: " +statusMessage);

                switch(statusCode) {
                    case 1: // HTTP Status 200
                        break;
                    case 3: // HTTP Status 401 (Invalid API key)
                        return null;
                    case 6: // HTTP Status 404 (Invalid id)
                        return null;
                    default:
                        return null;
                }
            }

            int movieId = trailerJSONObject.getInt(MOVIE_ID);
            JSONArray trailerResultsArray = trailerJSONObject.getJSONArray(RESULTS);

            for (int i = 0; i < trailerResultsArray.length(); i++) {
                JSONObject results = trailerResultsArray.getJSONObject(i);
                String trailer_key = results.optString(MOVIE_TRAILER_KEY);
                String trailer_name = results.optString(MOVIE_TRAILER_NAME);
                Trailer trailer = new Trailer(movieId, trailer_key, trailer_name);
                trailerList.add(trailer);
            }
        } catch (JSONException e) {
            Log.e(TAG, JSON_PARSE_ERROR, e);
        }
        return trailerList;
    }

    /**
     * Parses JSON response from the "themoviedb.org" reviews API and returns a list of {@link Review} objects.
     * <br>
     * @param reviewsJSONResponse JSON response from server.
     * @return List of {@link Trailer} Review.
     */
    public static List<Review> extractReviewsFromJsonResponse(String reviewsJSONResponse) {
        if (TextUtils.isEmpty(reviewsJSONResponse)) {
            return null;
        }
        List<Review> reviewList = new ArrayList<>();

        try {
            JSONObject reviewJSONObject = new JSONObject(reviewsJSONResponse);

            // https://www.themoviedb.org/documentation/api/status-codes?language=en-US
            if(reviewJSONObject.has(STATUS_CODE)) {
                int statusCode = reviewJSONObject.getInt(STATUS_CODE);
                String statusMessage = reviewJSONObject.optString(STATUS_MESSAGE);
                Log.d(TAG, "Status code: " +statusCode +"Status message: " +statusMessage);

                switch(statusCode) {
                    case 1: // HTTP Status 200
                        break;
                    case 3: // HTTP Status 401 (Invalid API key)
                        return null;
                    case 6: // HTTP Status 404 (Invalid id)
                        return null;
                    default:
                        return null;
                }
            }

            int movieId = reviewJSONObject.getInt(MOVIE_ID);
            JSONArray trailerResultsArray = reviewJSONObject.getJSONArray(RESULTS);

            for (int i = 0; i < trailerResultsArray.length(); i++) {
                JSONObject results = trailerResultsArray.getJSONObject(i);
                String review_author = results.optString(MOVIE_REVIEW_AUTHOR);
                String review_content = results.optString(MOVIE_REVIEW_CONTENT);
                Review review = new Review(movieId, review_author, review_content);
                reviewList.add(review);
            }
        } catch (JSONException e) {
            Log.e(TAG, JSON_PARSE_ERROR, e);
        }
        return reviewList;
    }
}
