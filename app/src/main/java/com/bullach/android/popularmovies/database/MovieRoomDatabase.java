package com.bullach.android.popularmovies.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

import com.bullach.android.popularmovies.models.Movie;
import com.bullach.android.popularmovies.database.dao.MovieDao;

@Database(entities = {Movie.class}, version = 1, exportSchema = false)
public abstract class MovieRoomDatabase extends RoomDatabase {

    private static final String LOG_TAG = MovieRoomDatabase.class.getSimpleName();
    private static final String DATABASE_NAME = "movie_database";

    private static volatile MovieRoomDatabase INSTANCE;

    public static MovieRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MovieRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MovieRoomDatabase.class, MovieRoomDatabase.DATABASE_NAME)
                            .build();
                }
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return INSTANCE;
    }

    public abstract MovieDao movieDao();
}
