package com.okeefe.peter.movieapp;

import android.accounts.Account;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.okeefe.peter.movieapp.data.MoviePosterDataContract;
import com.okeefe.peter.movieapp.sync.MovieContentSyncAdapter;
import com.okeefe.peter.movieapp.sync.SyncService;

/**
 * Created by peterokeefe on 8/26/15.
 */
public class TestSyncAdapter extends AndroidTestCase {


    public void testSync() {

        getContext().getContentResolver().registerContentObserver(MoviePosterDataContract.CONTENT_URI, true, new ContentObserver(null) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                Log.i("TestSyncAdapter", "Changed identified");
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
            }
        });


        MovieContentSyncAdapter.syncImmediately(getContext());


    }


    public void testReadMovies() {

        assertNotNull(MovieContentSyncAdapter.getMoviesArray(1, getContext()));

        assertNotNull(MovieContentSyncAdapter.getGenresArray(getContext()));


    }


}
