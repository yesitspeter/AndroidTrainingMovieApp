package com.okeefe.peter.movieapp.data;

import android.content.ContentResolver;
import android.graphics.Movie;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by peterokeefe on 8/25/15.
 */
public class MoviePosterDataContract {

    public static final String SORT_ORDER_POPULARITY =
            MovieEntry.POPULARITY + " DESC";
    public static final String SORT_ORDER_VOTE =
            MovieEntry.VOTE_AVERAGE + " DESC";


    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +

                    MovieEntry._ID + " INTEGER PRIMARY KEY   AUTOINCREMENT," +
                    MovieEntry.TITLE + " TEXT," +
                    MovieEntry.LANGUAGE + " TEXT," +
                    MovieEntry.POSTER_THUMBNAIL + " TEXT," +
                    MovieEntry.BACKDROP_PATH + " TEXT," +
                    MovieEntry.OVERVIEW + " TEXT," +
                    MovieEntry.REMOTE_ID + " INTEGER," +
                    MovieEntry.VOTE_AVERAGE + " REAL," +
                    MovieEntry.FAVORITE + " INTEGER," +
                    MovieEntry.POPULARITY + " REAL," +
                    MovieEntry.RELEASE_DATE + " INTEGER  );";


    public static final String SQL_CREATE_GENRE_REL =
            "CREATE TABLE " + MovieEntryGenre.TABLE_NAME + " (" +

                    MovieEntryGenre._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MovieEntryGenre.GENRE_ID + " INTEGER," +
                    MovieEntryGenre.MOVIE_ID + " INTEGER," +
                    "FOREIGN KEY(" + MovieEntryGenre.GENRE_ID + ") REFERENCES " + GenreEntry.TABLE_NAME + "(" + GenreEntry._ID + ")," +
                    "FOREIGN KEY(" + MovieEntryGenre.MOVIE_ID + ") REFERENCES " + MovieEntry.TABLE_NAME + "(" + MovieEntry._ID + ")  UNIQUE(" + MovieEntryGenre.GENRE_ID + "," + MovieEntryGenre.MOVIE_ID + ") ON CONFLICT REPLACE);";


    public static final String SQL_CREATE_GENRE =
            "CREATE TABLE " + GenreEntry.TABLE_NAME + " (" +
                    GenreEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    GenreEntry.GENRE_NAME + " TEXT);";


    public static final String MOVIE_AUTHORITY = "com.okeefe.peter.movieapp.sync.provider";

    public static final Uri CONTENT_URI =
            Uri.parse("content://" + MOVIE_AUTHORITY);


    public static abstract class MovieEntry implements BaseColumns {


        public static final String URI_PATH = "/movie";


        public static final String MIME_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + URI_PATH;
        public static final String MIME_TYPE_LIST = ContentResolver.CURSOR_DIR_BASE_TYPE + URI_PATH;

        public static final String TABLE_NAME = "movies";
        public static final String POPULARITY = "popularity";
        public static final String FAVORITE = "favorite";

        public static final String LANGUAGE = "language";
        public static final String TITLE = "title";
        public static final String POSTER_THUMBNAIL = "thumbnail";
        public static final String VOTE_AVERAGE = "vote_average";
        public static final String RELEASE_DATE = "release_date";

        public static final String OVERVIEW = "plot_synopsis";


        public static final String BACKDROP_PATH = "backdrop_path";
        public static final String REMOTE_ID = "remote_id";
    }


    public static abstract class MovieEntryGenre implements BaseColumns {

        public static final String URI_PATH = "/genrerelation";
        public static final String MIME_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + URI_PATH;
        public static final String MIME_TYPE_LIST = ContentResolver.CURSOR_DIR_BASE_TYPE + URI_PATH;

        public static final String TABLE_NAME = "movie_to_genre";
        public static final String GENRE_ID = "genre_reference";
        public static final String MOVIE_ID = "movie_reference";

    }


    public static abstract class GenreEntry implements BaseColumns {

        public static final String URI_PATH = "/genre";
        public static final String MIME_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + URI_PATH;
        public static final String MIME_TYPE_LIST = ContentResolver.CURSOR_DIR_BASE_TYPE + URI_PATH;

        public static final String TABLE_NAME = "genre";
        public static final String GENRE_NAME = "name";

    }
}
