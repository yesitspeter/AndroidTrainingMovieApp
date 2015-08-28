package com.okeefe.peter.movieapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by peterokeefe on 8/25/15.
 */
public class MoviePosterDbHelper extends SQLiteOpenHelper {


    public static final int DATABASE_VERSION = 12;
    public static final String DATABASE_NAME = "MoviePoster.db";


    public MoviePosterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL(MoviePosterDataContract.SQL_CREATE_ENTRIES);
        db.execSQL(MoviePosterDataContract.SQL_CREATE_GENRE);
        db.execSQL(MoviePosterDataContract.SQL_CREATE_GENRE_REL);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE " + MoviePosterDataContract.MovieEntry.TABLE_NAME + ";");
        db.execSQL("DROP TABLE " + MoviePosterDataContract.MovieEntryGenre.TABLE_NAME + ";");
        db.execSQL("DROP TABLE " + MoviePosterDataContract.GenreEntry.TABLE_NAME + ";");

        onCreate(db);

    }
}
