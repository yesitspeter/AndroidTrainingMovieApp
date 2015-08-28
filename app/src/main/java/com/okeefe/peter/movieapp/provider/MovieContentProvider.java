package com.okeefe.peter.movieapp.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.v4.database.DatabaseUtilsCompat;
import android.text.TextUtils;
import android.util.Log;

import com.okeefe.peter.movieapp.data.MoviePosterDataContract;
import com.okeefe.peter.movieapp.data.MoviePosterDbHelper;

import org.w3c.dom.Text;

/**
 * Created by peterokeefe on 8/25/15.
 */
public class MovieContentProvider extends ContentProvider {


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int URI_MOVIES = 100;
    private static final int URI_MOVIE_ID = 101;
    private static final int URI_GENRES = 110;
    private static final int URI_GENRE_ID = 111;

    private static final int URI_MOVIES_BY_TITLE = 102;


    private static final int URI_GENRE_MOVIE_ID = 113;
    private static final String LOG_KEY = "MovieContentProvider";


    private MoviePosterDbHelper mDbHelper;


    static {


        sUriMatcher.addURI(MoviePosterDataContract.MOVIE_AUTHORITY, "movies", URI_MOVIES);
        sUriMatcher.addURI(MoviePosterDataContract.MOVIE_AUTHORITY, "movies/#", URI_MOVIE_ID);
        sUriMatcher.addURI(MoviePosterDataContract.MOVIE_AUTHORITY, "movies/*", URI_MOVIES_BY_TITLE);

        sUriMatcher.addURI(MoviePosterDataContract.MOVIE_AUTHORITY, "genres", URI_GENRES);
        sUriMatcher.addURI(MoviePosterDataContract.MOVIE_AUTHORITY, "genres/#", URI_GENRE_ID);


        sUriMatcher.addURI(MoviePosterDataContract.MOVIE_AUTHORITY, "genresbymovie/#", URI_GENRE_MOVIE_ID);

    }

    public MovieContentProvider() {
        super();
    }

    @Override
    public boolean onCreate() {


        mDbHelper = new MoviePosterDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {


        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();


        switch (sUriMatcher.match(uri)) {

            case URI_MOVIES_BY_TITLE:

                builder.setTables(MoviePosterDataContract.MovieEntry.TABLE_NAME);


                builder.appendWhere(MoviePosterDataContract.MovieEntry.TITLE + " = ");
                builder.appendWhereEscapeString(uri.getLastPathSegment());


                break;


            case URI_MOVIES:

                builder.setTables(MoviePosterDataContract.MovieEntry.TABLE_NAME);


                break;

            case URI_MOVIE_ID:

                builder.setTables(MoviePosterDataContract.MovieEntry.TABLE_NAME);

                builder.appendWhere(MoviePosterDataContract.MovieEntry._ID + " = ");


                builder.appendWhereEscapeString(uri.getLastPathSegment());

                break;

            case URI_GENRES:

                builder.setTables(MoviePosterDataContract.GenreEntry.TABLE_NAME);


                break;

            case URI_GENRE_ID:


                builder.setTables(MoviePosterDataContract.GenreEntry.TABLE_NAME);

                builder.appendWhere(MoviePosterDataContract.GenreEntry._ID + " = ");


                builder.appendWhereEscapeString(uri.getLastPathSegment());
                break;

            case URI_GENRE_MOVIE_ID:

                builder.setTables(MoviePosterDataContract.GenreEntry.TABLE_NAME + " NATURAL JOIN " + MoviePosterDataContract.MovieEntryGenre.TABLE_NAME);


                builder.appendWhere(MoviePosterDataContract.MovieEntryGenre.MOVIE_ID + " = ");


                builder.appendWhereEscapeString(uri.getLastPathSegment());


                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI: " + uri);

        }
        Log.i(LOG_KEY, "Executing " + builder.toString());
        Cursor cursor =
                builder.query(
                        db,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

        cursor.setNotificationUri(
                getContext().getContentResolver(),
                uri);

        return cursor;


    }

    @Override
    public String getType(Uri uri) {

        switch (sUriMatcher.match(uri)) {

            case URI_MOVIES_BY_TITLE:
            case URI_MOVIES:

                return MoviePosterDataContract.MovieEntry.MIME_TYPE_LIST;

            case URI_MOVIE_ID:

                return MoviePosterDataContract.MovieEntry.MIME_TYPE_ITEM;


            case URI_GENRES:
                return MoviePosterDataContract.GenreEntry.MIME_TYPE_LIST;


            case URI_GENRE_ID:

                return MoviePosterDataContract.GenreEntry.MIME_TYPE_ITEM;

            case URI_GENRE_MOVIE_ID:
                return MoviePosterDataContract.GenreEntry.MIME_TYPE_LIST;

            default:
                return null;
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri result = null;
        switch (sUriMatcher.match(uri)) {
            case URI_MOVIES: {
                long id = db.insert(MoviePosterDataContract.MovieEntry.TABLE_NAME, null, values);

                result = Uri.parse(MoviePosterDataContract.CONTENT_URI + "/movies/" + id);

                break;
            }

            case URI_GENRES: {
                long id = db.insert(MoviePosterDataContract.GenreEntry.TABLE_NAME, null, values);

                result = Uri.parse(MoviePosterDataContract.CONTENT_URI + "/genres/" + id);


                break;
            }


            case URI_GENRE_MOVIE_ID:

                long genreLong = values.getAsLong(MoviePosterDataContract.MovieEntryGenre.GENRE_ID);
                long movieLong = Long.parseLong(uri.getLastPathSegment());


                ContentValues cvInsert = new ContentValues();
                cvInsert.put(MoviePosterDataContract.MovieEntryGenre.GENRE_ID, genreLong);
                cvInsert.put(MoviePosterDataContract.MovieEntryGenre.MOVIE_ID, movieLong);

                long id = db.insert(MoviePosterDataContract.MovieEntryGenre.TABLE_NAME, null, cvInsert);


                result = Uri.parse(MoviePosterDataContract.CONTENT_URI + "/genresbymovie/" + movieLong);


                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI: " + uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);

        return result;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String id;
        int rowsUpdated = 0;
        switch (sUriMatcher.match(uri)) {
            case URI_MOVIES:

                rowsUpdated = db.delete(MoviePosterDataContract.MovieEntry.TABLE_NAME, selection, selectionArgs);


                break;

            case URI_MOVIE_ID:

                String where = null;

                id = DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());
                if (!TextUtils.isEmpty(selection)) {

                    rowsUpdated = db.delete(MoviePosterDataContract.MovieEntry.TABLE_NAME, selection + " and (" + MoviePosterDataContract.MovieEntry._ID + " = " + id + ")", selectionArgs);


                } else {

                    rowsUpdated = db.delete(MoviePosterDataContract.MovieEntry.TABLE_NAME, "(" + MoviePosterDataContract.MovieEntry._ID + " = ? )", new String[]{uri.getLastPathSegment()});

                }


                break;

            case URI_GENRES:

                rowsUpdated = db.delete(MoviePosterDataContract.GenreEntry.TABLE_NAME, selection, selectionArgs);


                break;

            case URI_GENRE_ID:

                id = DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());

                if (!TextUtils.isEmpty(selection)) {

                    rowsUpdated = db.delete(MoviePosterDataContract.GenreEntry.TABLE_NAME, selection + " and (" + MoviePosterDataContract.GenreEntry._ID + " = " + id + ")", selectionArgs);


                } else {

                    rowsUpdated = db.delete(MoviePosterDataContract.GenreEntry.TABLE_NAME, "(" + MoviePosterDataContract.GenreEntry._ID + " = " + id + ")", selectionArgs);


                }

                break;

            case URI_GENRE_MOVIE_ID:

                String tables = (MoviePosterDataContract.GenreEntry.TABLE_NAME + " NATURAL JOIN " + MoviePosterDataContract.MovieEntryGenre.TABLE_NAME);
                id = DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());


                if (!TextUtils.isEmpty(selection)) {

                    rowsUpdated = db.delete(MoviePosterDataContract.MovieEntryGenre.TABLE_NAME, selection + " and (" + MoviePosterDataContract.MovieEntryGenre.MOVIE_ID + " = " + id + ")", selectionArgs);


                } else {

                    rowsUpdated = db.delete(MoviePosterDataContract.MovieEntryGenre.TABLE_NAME, "(" + MoviePosterDataContract.MovieEntryGenre.MOVIE_ID + " = " + id + ")", selectionArgs);

                }


                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI: " + uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);


        return rowsUpdated;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String id;
        int rowsUpdated = 0;
        switch (sUriMatcher.match(uri)) {
            case URI_MOVIES:

                rowsUpdated = db.update(MoviePosterDataContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);


                break;

            case URI_MOVIE_ID:

                String where = null;

                id = DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());
                if (!TextUtils.isEmpty(selection)) {

                    rowsUpdated = db.update(MoviePosterDataContract.MovieEntry.TABLE_NAME, values, selection + " and (" + MoviePosterDataContract.MovieEntry._ID + " = " + id + ")", selectionArgs);


                } else {

                    rowsUpdated = db.update(MoviePosterDataContract.MovieEntry.TABLE_NAME, values, "(" + MoviePosterDataContract.MovieEntry._ID + " = ? )", new String[]{uri.getLastPathSegment()});

                }


                break;

            case URI_GENRES:

                rowsUpdated = db.update(MoviePosterDataContract.GenreEntry.TABLE_NAME, values, selection, selectionArgs);


                break;

            case URI_GENRE_ID:

                id = DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());

                if (!TextUtils.isEmpty(selection)) {

                    rowsUpdated = db.update(MoviePosterDataContract.GenreEntry.TABLE_NAME, values, selection + " and (" + MoviePosterDataContract.GenreEntry._ID + " = " + id + ")", selectionArgs);


                } else {

                    rowsUpdated = db.update(MoviePosterDataContract.GenreEntry.TABLE_NAME, values, "(" + MoviePosterDataContract.GenreEntry._ID + " = " + id + ")", selectionArgs);

                }

                break;

            case URI_GENRE_MOVIE_ID:

                String tables = (MoviePosterDataContract.GenreEntry.TABLE_NAME + " NATURAL JOIN " + MoviePosterDataContract.MovieEntryGenre.TABLE_NAME);
                id = DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());


                if (!TextUtils.isEmpty(selection)) {

                    rowsUpdated = db.update(MoviePosterDataContract.GenreEntry.TABLE_NAME, values, selection + " and (" + MoviePosterDataContract.MovieEntryGenre.MOVIE_ID + " = " + id + ")", selectionArgs);


                } else {

                    rowsUpdated = db.update(MoviePosterDataContract.GenreEntry.TABLE_NAME, values, "(" + MoviePosterDataContract.MovieEntryGenre.MOVIE_ID + " = " + id + ")", selectionArgs);

                }


                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI: " + uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);


        return rowsUpdated;

    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        int numInserted = 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long result;

        db.beginTransaction();


        switch (sUriMatcher.match(uri)) {
            case URI_MOVIES: {

                for (ContentValues cv : values) {
                    result = db.insert(MoviePosterDataContract.MovieEntry.TABLE_NAME, null, cv);

                    if (result != -1)
                        ++numInserted;
                }
                break;
            }

            case URI_GENRES: {

                for (ContentValues cv : values) {

                    result = db.insert(MoviePosterDataContract.GenreEntry.TABLE_NAME, null, cv);

                    if (result != -1)
                        ++numInserted;
                }


                break;
            }


            default:
                throw new IllegalArgumentException(
                        "Unsupported URI: " + uri);

        }
        db.setTransactionSuccessful();
        db.endTransaction();
        getContext().getContentResolver().notifyChange(uri, null);


        return numInserted;


    }
}
