package com.bullach.android.popularmovies.utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.bullach.android.popularmovies.R;
import com.bullach.android.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving movies data from themoviedb.org API.
 */
public final class QueryUtils {

    /**
     * Tag for the log messages
     */
    private static final String TAG = QueryUtils.class.getSimpleName();

    /**
     * The HTTP method (GET, POST, PUT, etc.).
     */
    private static String method = "GET";

    /**
     * The read timeout in milliseconds for waiting to read data.
     */
    private static final int READ_TIMEOUT_IN_MS = 10000; /* milliseconds */

    /**
     * The connection timeout in milliseconds for making the initial connection.
     */
    private static final int CONNECTION_TIMEOUT_IN_MS = 15000;

    private static final String MOVIEDB_BASE_URL = "https://api.themoviedb.org";
    private static final String IMAGEDB_BASE_URL = "https://image.tmdb.org/t/p/";
    private final static String API_VERSION = "3";
    private static final String API_KEY_QUERY_PARAM = "api_key";
    public static final String POPULAR_MOVIE_PATH = "movie/popular";
    public static final String TOP_RATED_MOVIE_PATH = "movie/top_rated";
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
     * Builds the URL used to talk to the movie server.
     *
     * @param path The location that will be queried for.
     * @return The URL to use to query the movie server.
     */
    public static URL buildUrl(Context context, @NonNull String path) {
        Uri builtUri = Uri.parse(QueryUtils.MOVIEDB_BASE_URL).buildUpon()
                .appendEncodedPath(QueryUtils.API_VERSION)
                .appendEncodedPath(path)
                .appendQueryParameter(QueryUtils.API_KEY_QUERY_PARAM, context.getString(R.string.the_moviedb_api_key))
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
        return Uri.parse(IMAGEDB_BASE_URL).buildUpon()
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
            jsonResponse = makeHttpRequest(url);
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
    private static String makeHttpRequest(URL url) throws IOException {
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
                // the makeHttpRequest(URL url) method signature specifies than an IOException
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

    /**
     * Return a list of {@link Movie} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Movie> extractMoviesFromJson(String movieJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(movieJSON)) {
            return null;
        }
        // Create an empty ArrayList that we can start adding movies to
        List<Movie> movieList = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Extract the JSONArray associated with the key called "results", which represents a list of movies.
            JSONArray movieResultsArray = new JSONObject(movieJSON).getJSONArray("results");

            // For each movie in the movieResultsArray, create an {@link Movie} object
            for (int i = 0; i < movieResultsArray.length(); i++) {
                // Get a single movie object at position i within the list of movies
                JSONObject results = movieResultsArray.getJSONObject(i);
                // Extract the movie id value for the key called "id"
                int id = results.optInt("id");
                // Extract the movie title value for the key called "title"
                String title = results.optString("title");
                // Extract the path for the movie poster image thumbnail
                String poster_path = results.optString("poster_path");
                // Extract the movie plot synopsis
                String overview = results.optString("overview");
                // Extract the movie average vote
                String vote_average = results.optString("vote_average");
                // Extract the movie release date
                String release_date = results.optString("release_date");
                // Create a new {@link Movie} object with the extracted values from the JSON response.
                Movie movies = new Movie(id, title, poster_path, overview, vote_average, release_date);
                // Add the new {@link Movies} to the list of movies.
                movieList.add(movies);
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the movies JSON results", e);
        }
        // Return the list of movies
        return movieList;
    }
}
