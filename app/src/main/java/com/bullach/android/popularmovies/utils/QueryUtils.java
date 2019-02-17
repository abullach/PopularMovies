package com.bullach.android.popularmovies.utils;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bullach.android.popularmovies.models.Movie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import static com.bullach.android.popularmovies.utils.MovieJSONUtils.extractMoviesFromJson;

/**
 * Helper methods related to requesting and receiving movies data from themoviedb.org API.
 */
public final class QueryUtils {

    /**
     * Tag for the log messages
     */
    private static final String TAG = QueryUtils.class.getSimpleName();

    /**
     * The read timeout in milliseconds for waiting to read data.
     */
    private static final int READ_TIMEOUT_IN_MS = 10000; /* milliseconds */

    /**
     * The connection timeout in milliseconds for making the initial connection.
     */
    private static final int CONNECTION_TIMEOUT_IN_MS = 15000;

    private static final String API_VERSION = "3";
    private static final String API_KEY_QUERY_PARAM = "api_key";
    private static final String MOVIE = "movie";
    private static final String VIDEOS = "videos";
    private static final String REVIEWS = "reviews";
    public static final String SORT_TYPE_POPULAR = "popular";
    public static final String SORT_TYPE_TOP_RATED = "top_rated";
    public static final String POSTER_IMAGE_SIZE_W185_PATH = "w185";
    public static final String POSTER_IMAGE_SIZE_W500_PATH = "w500";

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Builds the URL used to get movie details from the 'themoviedb.org' API.
     *
     * @param sortType The sortType to query the movie details API.
     * @return The URL to use to query the movie server.
     */
    public static URL buildMovieUrl(@NonNull String sortType) {
        Uri builtUri = Uri.parse(Constants.MOVIEDB_BASE_URL).buildUpon()
                .appendEncodedPath(QueryUtils.API_VERSION)
                .appendEncodedPath(MOVIE)
                .appendEncodedPath(sortType)
                .appendQueryParameter(QueryUtils.API_KEY_QUERY_PARAM, Constants.API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Built URI " + url);
        return url;
    }

    public static URL buildTrailersUrl(@NonNull String id) {
        Uri builtUri = Uri.parse(Constants.MOVIEDB_BASE_URL).buildUpon()
                .appendEncodedPath(QueryUtils.API_VERSION)
                .appendEncodedPath(MOVIE)
                .appendEncodedPath(id)
                .appendEncodedPath(VIDEOS)
                .appendQueryParameter(QueryUtils.API_KEY_QUERY_PARAM, Constants.API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Built URI " + url);
        return url;
    }

    public static URL buildReviewsUrl(@NonNull String id) {
        Uri builtUri = Uri.parse(Constants.MOVIEDB_BASE_URL).buildUpon()
                .appendEncodedPath(QueryUtils.API_VERSION)
                .appendEncodedPath(MOVIE)
                .appendEncodedPath(id)
                .appendEncodedPath(REVIEWS)
                .appendQueryParameter(QueryUtils.API_KEY_QUERY_PARAM, Constants.API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Built URI " + url);
        return url;
    }

    public static Uri buildPosterImageUrl(@NonNull String moviePosterFilePath, @NonNull String moviePosterSizePath) {
        return Uri.parse(Constants.IMAGEDB_BASE_URL).buildUpon()
                .appendEncodedPath(moviePosterSizePath)
                .appendEncodedPath(moviePosterFilePath)
                .build();
    }

    /**
     * Query the 'themoviedb.org' dataset and return a list of {@link Movie} objects.
     */
    public static List<Movie> fetchMovieData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = getResponseFromHttpUrl(url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Movie}s
        List<Movie> movies = extractMoviesFromJson(jsonResponse);

        // Return the list of {@link Movie}s
        return movies;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(READ_TIMEOUT_IN_MS);
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT_IN_MS);

            String method = "GET";
            urlConnection.setRequestMethod(method);
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving the movie JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the getResponseFromHttpUrl(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
