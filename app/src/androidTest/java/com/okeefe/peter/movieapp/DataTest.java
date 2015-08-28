package com.okeefe.peter.movieapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Movie;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;

import com.okeefe.peter.movieapp.data.MoviePosterDataContract;
import com.okeefe.peter.movieapp.data.MoviePosterDbHelper;
import com.okeefe.peter.movieapp.provider.MovieContentProvider;
import com.okeefe.peter.movieapp.sync.MovieContentSyncAdapter;

import java.util.Hashtable;

/**
 * Created by peterokeefe on 8/26/15.
 */
public class DataTest extends ProviderTestCase2<MovieContentProvider> {


    public static String LOG_KEY = "DataTest";

    public DataTest() {
        super(MovieContentProvider.class, MoviePosterDataContract.MOVIE_AUTHORITY);
    }

    private ContentValues getContentForMovieEntry() {

        ContentValues cv = new ContentValues();
        cv.put(MoviePosterDataContract.MovieEntry.TITLE, "Title" + Math.random());
        cv.put(MoviePosterDataContract.MovieEntry.VOTE_AVERAGE, Math.random());

        cv.put(MoviePosterDataContract.MovieEntry.RELEASE_DATE, "11-24-2009");
        cv.put(MoviePosterDataContract.MovieEntry.POSTER_THUMBNAIL, "http://somewhere.com/animage.jpg");
        cv.put(MoviePosterDataContract.MovieEntry.POPULARITY, Math.random());
        cv.put(MoviePosterDataContract.MovieEntry.LANGUAGE, "en");
        cv.put(MoviePosterDataContract.MovieEntry.OVERVIEW, "Stuff happens here ");

        return cv;

    }


    public void testInsertDelete() {

        Uri u = getMockContentResolver().insert(MoviePosterDataContract.CONTENT_URI.buildUpon().appendPath("movies").build(), getContentForMovieEntry());


        assertNotNull(u);
        assertNotSame(u.getLastPathSegment(), "-1");

        Log.i(LOG_KEY, u.toString());


        int numDeleted = getMockContentResolver().delete(u, null, null);

        assertEquals("We should have deleted an entry", numDeleted, 1);


    }


    public void testInsertGenereOfMovie() throws Exception {

        Uri u = getMockContentResolver().insert(MoviePosterDataContract.CONTENT_URI.buildUpon().appendPath("movies").build(), getContentForMovieEntry());


        ContentValues genre, genreToMovie;

        genre = new ContentValues();

        genre.put(MoviePosterDataContract.GenreEntry.GENRE_NAME, "Horrible Films");


        Uri genreUri = getMockContentResolver().insert(MoviePosterDataContract.CONTENT_URI.buildUpon().appendPath("genres").build(), genre);


        long genreId = Long.valueOf(u.getLastPathSegment());


        genreToMovie = new ContentValues();
        genreToMovie.put(MoviePosterDataContract.MovieEntryGenre.GENRE_ID, genreId);


        Uri genreEntryUri = getMockContentResolver().insert(MoviePosterDataContract.CONTENT_URI.buildUpon().appendPath("genresbymovie").appendPath(u.getLastPathSegment()).build(), genreToMovie);

        Log.i(LOG_KEY, "Genere entry uri is " + genreEntryUri.toString());

        Cursor c = getMockContentResolver().query(MoviePosterDataContract.CONTENT_URI.buildUpon().appendPath("genresbymovie").appendPath(u.getLastPathSegment()).build(),
                null, null, null, null);


        assertTrue(c.moveToFirst());

        assertEquals(c.getString(c.getColumnIndex(MoviePosterDataContract.GenreEntry.GENRE_NAME)), "Horrible Films");


        Hashtable<Long, String> ls = MovieContentSyncAdapter.getIdsToGenreNames(getMockContentResolver().acquireContentProviderClient(MoviePosterDataContract.CONTENT_URI));


        assertEquals(ls.size(), 1);


        int numDeleted = getMockContentResolver().delete(u, null, null);

        assertEquals("We should have deleted an entry", numDeleted, 1);


        numDeleted = getMockContentResolver().delete(genreEntryUri, null, null);

        assertEquals("We should have deleted an entry", numDeleted, 1);

        numDeleted = getMockContentResolver().delete(genreUri, null, null);

        assertEquals("We should have deleted an entry", numDeleted, 1);
    }

    public void testSyncGenres() {

        MovieContentSyncAdapter.syncGenres(getMockContentResolver().acquireContentProviderClient(MoviePosterDataContract.CONTENT_URI), getContext());
        MovieContentSyncAdapter.syncGenres(getMockContentResolver().acquireContentProviderClient(MoviePosterDataContract.CONTENT_URI), getContext());


    }

    public void testSyncMovies() {

        MovieContentSyncAdapter.syncMovies(getMockContentResolver().acquireContentProviderClient(MoviePosterDataContract.CONTENT_URI), getContext());
        MovieContentSyncAdapter.syncMovies(getMockContentResolver().acquireContentProviderClient(MoviePosterDataContract.CONTENT_URI), getContext());


    }


}
