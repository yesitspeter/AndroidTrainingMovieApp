package com.okeefe.peter.movieapp.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.JsonReader;
import android.util.Log;

import com.okeefe.peter.movieapp.R;
import com.okeefe.peter.movieapp.SettingsConstants;
import com.okeefe.peter.movieapp.data.MoviePosterDataContract;
import com.okeefe.peter.movieapp.data.MoviePosterDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;


/**
 * Created by peterokeefe on 8/25/15.
 */
public class MovieContentSyncAdapter extends AbstractThreadedSyncAdapter {

    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;

    private final static String LOG_KEY = "MovieContentSyncAdapter";

    /**
     * Set up the sync adapter
     */
    public MovieContentSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public MovieContentSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }


    public static void syncMovies(ContentProviderClient client, Context context) {


        JSONArray moviesArray = getMoviesArray(1, context);
        if (moviesArray == null) {
            Log.e(LOG_KEY, "No movies were returned!");

            return;
        }
        try {
            for (int i = 0; i < moviesArray.length(); ++i) {

                JSONObject movieEntry = moviesArray.getJSONObject(i);

                long remoteId = movieEntry.getLong("id");
                boolean exists = movieExistsWithRemoteId(client, remoteId);

                ContentValues movieValues = movieEntryToContentValues(movieEntry);

                if (exists) {
                    Uri u = MoviePosterDataContract.CONTENT_URI.buildUpon().appendPath("movies").build();


                    client.update(u, movieValues, MoviePosterDataContract.MovieEntry.REMOTE_ID + " = ?", new String[]{Long.toString(remoteId)});


                } else {
                    Uri u = MoviePosterDataContract.CONTENT_URI.buildUpon().appendPath("movies").build();

                    client.insert(u, movieValues);

                }

            }

        } catch (JSONException je) {
            Log.wtf(LOG_KEY, je);

        } catch (RemoteException ro) {
            Log.wtf(LOG_KEY, ro);


        }


    }

    private static ContentValues movieEntryToContentValues(JSONObject movieEntry) throws JSONException {


        ContentValues out = new ContentValues();

        out.put(MoviePosterDataContract.MovieEntry.LANGUAGE, movieEntry.getString("original_language"));
        out.put(MoviePosterDataContract.MovieEntry.OVERVIEW, movieEntry.getString("overview"));
        out.put(MoviePosterDataContract.MovieEntry.POPULARITY, movieEntry.getDouble("popularity"));
        out.put(MoviePosterDataContract.MovieEntry.POSTER_THUMBNAIL, movieEntry.getString("poster_path"));
        out.put(MoviePosterDataContract.MovieEntry.RELEASE_DATE, Date.valueOf(movieEntry.getString("release_date")).getTime());
        out.put(MoviePosterDataContract.MovieEntry.TITLE, movieEntry.getString("original_title"));
        out.put(MoviePosterDataContract.MovieEntry.VOTE_AVERAGE, movieEntry.getDouble("vote_average"));
        out.put(MoviePosterDataContract.MovieEntry.BACKDROP_PATH, movieEntry.getString("backdrop_path"));

        out.put(MoviePosterDataContract.MovieEntry.REMOTE_ID, movieEntry.getString("id"));


        return out;
    }


    public static JSONArray getMoviesArray(int pageId, Context context) {

        try {
            Uri.Builder base = Uri.parse("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc").buildUpon();

            base.appendQueryParameter("api_key", context.getString(R.string.api_key_tmdb));
            base.appendQueryParameter("page", Integer.toString(pageId));

            HttpURLConnection urlConnection = (HttpURLConnection) new URL(base.build().toString()).openConnection();


            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/json");

            if (urlConnection.getResponseCode() != 200) {

                Log.e(LOG_KEY, "Bad response from server when getting genres: " + urlConnection.getResponseCode());


                return null;
            }

            InputStream in = null;


            try {

                in = urlConnection.getInputStream();

                String jsonString = readString(in);
                JSONObject jo = new JSONObject(jsonString);


                JSONArray movies = jo.getJSONArray("results");

                return movies;

            } catch (Exception e) {


                Log.wtf(LOG_KEY, e);

            } finally {

                if (in != null)
                    in.close();
                ;


                urlConnection.disconnect();


            }

        } catch (Exception e) {
            Log.wtf(LOG_KEY, e);

        }
        return null;
    }

    public static JSONArray getGenresArray(Context context) {

        try {

            Uri.Builder base = Uri.parse("http://api.themoviedb.org/3/genre/movie/list").buildUpon();

            base.appendQueryParameter("api_key", context.getString(R.string.api_key_tmdb));

            HttpURLConnection urlConnection = (HttpURLConnection) new URL(base.build().toString()).openConnection();

            try {
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Accept", "application/json");

                if (urlConnection.getResponseCode() != 200) {

                    Log.e(LOG_KEY, "Bad response from server when getting genres: " + urlConnection.getResponseCode());


                    return null;
                }

                InputStream in = null;


                try {

                    in = urlConnection.getInputStream();


                    String jsonString = readString(in);

                    JSONObject jo = new JSONObject(jsonString);


                    JSONArray genresArray = jo.getJSONArray("genres");

                    return genresArray;

                } finally {

                    if (in != null)
                        in.close();
                    ;


                    urlConnection.disconnect();


                }

            } catch (Exception e) {
                Log.wtf(LOG_KEY, e);

            }

        } catch (Exception mef) {
            Log.wtf(LOG_KEY, mef);


        }
        return null;
    }

    public static String readString(final InputStream is) {
        final char[] buffer = new char[4096];
        final StringBuilder out = new StringBuilder();
        try {

            Reader in = new InputStreamReader(is, "UTF-8");
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
        } catch (UnsupportedEncodingException ex) {
        /* ... */
        } catch (IOException ex) {
        /* ... */
        }
        return out.toString();
    }

    public static void syncGenres(ContentProviderClient client, Context context) {


        JSONArray genresArray = getGenresArray(context);
        if (genresArray == null)
            return;

        Hashtable<Long, String> genreSet = null;
        try {
            genreSet = getIdsToGenreNames(client);
        } catch (RemoteException e) {

            Log.wtf(LOG_KEY, "Could not read genres from database", e);
            return;
        }
        try {

            ArrayList<ContentValues> cvList = new ArrayList<>();

            for (int i = 0; i < genresArray.length(); ++i) {

                JSONObject genreObject = genresArray.getJSONObject(i);

                String genreName = genreObject.getString("name");
                long genreId = genreObject.getLong("id");


                if (genreSet.containsKey(genreId)) {

                    if (!genreSet.get(genreId).equals(genreName)) {
                        ContentValues cv = new ContentValues();

                        cv.put(MoviePosterDataContract.GenreEntry.GENRE_NAME, genreName);

                        client.update(Uri.parse(MoviePosterDataContract.CONTENT_URI + "/genres/" + genreId), cv, null, null);
                    }
                } else {

                    ContentValues cv = new ContentValues();

                    cv.put(MoviePosterDataContract.GenreEntry.GENRE_NAME, genreName);
                    cv.put(MoviePosterDataContract.GenreEntry._ID, genreId);

                    cvList.add(cv);
                }


            }
            client.bulkInsert(Uri.parse(MoviePosterDataContract.CONTENT_URI + "/genres"), cvList.toArray(new ContentValues[cvList.size()]));

        } catch (JSONException e) {
            Log.wtf(LOG_KEY, "Could not read json", e);
            return;

        } catch (RemoteException re) {


            Log.wtf(LOG_KEY, "Remote Exception", re);
            return;

        }


    }

    @NonNull
    public static Hashtable<Long, String> getIdsToGenreNames(ContentProviderClient client) throws RemoteException {


        Cursor cursorForGenres = null;
        try {
            String[] projection = new String[]{
                    MoviePosterDataContract.GenreEntry._ID,
                    MoviePosterDataContract.GenreEntry.GENRE_NAME

            };
            Hashtable<Long, String> genreSet = new Hashtable<Long, String>();

            cursorForGenres = client.query(Uri.parse(MoviePosterDataContract.CONTENT_URI + "/genres"), projection, null, null, null);

            if (cursorForGenres.moveToFirst())
                for (; !cursorForGenres.isAfterLast(); cursorForGenres.moveToNext()) {


                    long id = cursorForGenres.getLong(0);
                    String name = cursorForGenres.getString(1);

                    genreSet.put(id, name);


                }
            return genreSet;
        } finally {

            if (cursorForGenres != null)
                cursorForGenres.close();
        }

    }


    private static boolean movieExistsWithRemoteId(ContentProviderClient client, long remoteId) throws RemoteException {

        String[] projection = new String[]{
                MoviePosterDataContract.GenreEntry._ID


        };
        Cursor cursor = null;

        try {
            Uri u = Uri.parse(MoviePosterDataContract.CONTENT_URI + "/movies");
            cursor = client.query(u, projection, MoviePosterDataContract.MovieEntry.REMOTE_ID + " = ?", new String[]{Long.toString(remoteId)}, null);


            return cursor.moveToFirst();

        } finally {

            if (cursor != null)
                cursor.close();
        }

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        syncGenres(provider, getContext());

        getContext().getSharedPreferences(SettingsConstants.SHARED_PREFERENCES_NAME, 0);


        syncMovies(provider, getContext());


    }


    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        }
        return newAccount;
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        // ContentResolver.setIsSyncable(getSyncAccount(context), context.getString(R.string.content_authority), 1);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }


}
