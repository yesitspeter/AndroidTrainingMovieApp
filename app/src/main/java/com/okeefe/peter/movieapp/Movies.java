package com.okeefe.peter.movieapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ShareActionProvider;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.okeefe.peter.movieapp.data.MoviePosterDataContract;
import com.okeefe.peter.movieapp.detailsview.DetailActivity;
import com.okeefe.peter.movieapp.detailsview.MovieDetailsFragment;


public class Movies extends AppCompatActivity implements PostersFragment.PosterSelectedCallback  {


    private static final String LOG_KEY = "MoviesActivity";
    private static final String SAVED_SORT = "savedSortPos";
    private android.support.v7.widget.ShareActionProvider mShareActionProvider;
    private MovieDetailsFragment mDetailsFragment;
    private Spinner mSpinner;

    private int mSpinnerPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);


        mDetailsFragment = (MovieDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.movie_detail_fragment);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

        Spinner s = (Spinner) menu.findItem(R.id.action_sort).getActionView();     // find the spinner
        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this.getSupportActionBar().getThemedContext(), R.array.sort_spinner_list, android.R.layout.simple_spinner_dropdown_item);    // create the adapter from a StringArray
        s.setAdapter(mSpinnerAdapter);   // set the adapter


        if (mSpinnerPosition != -1)
            s.setSelection(mSpinnerPosition);

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PostersFragment f = (PostersFragment) getSupportFragmentManager().findFragmentById(R.id.posters_fragment);
                if (f == null)
                    Log.w(LOG_KEY, "Could not find posters fragment");
                else
                    setSortPosition(position, f);

                mSpinnerPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpinner = s;




        return true;
    }

    private void setSortPosition(int position, PostersFragment f) {
        if (position == 0) {
            f.setSortOrderAndFavoritesOnly(false, MoviePosterDataContract.SORT_ORDER_POPULARITY);

        } else if (position == 1) {

            f.setSortOrderAndFavoritesOnly(false, MoviePosterDataContract.SORT_ORDER_VOTE);


        } else if (position == 2) {
            f.setSortOrderAndFavoritesOnly(true, null);

        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //   return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri movieUri, String remoteId) {
        if (mDetailsFragment != null) {
            View detailsContainer = findViewById(R.id.detail_fragment_tab_container);
            detailsContainer.setVisibility(View.VISIBLE);
            mDetailsFragment.setMovieId(movieUri, remoteId);

            GridView gv = (GridView) findViewById(R.id.posters_poster_gridview);
            gv.setNumColumns(1);
        } else {

            Intent detailIntent = new Intent(this, DetailActivity.class);
            Bundle extras = new Bundle();

            extras.putString(MovieDetailsFragment.MOVIE_ID_KEY, remoteId);
            extras.putString(MovieDetailsFragment.MOVIE_URI_KEY, movieUri.toString());
            detailIntent.putExtras(extras);
            startActivity(detailIntent);
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt(SAVED_SORT, mSpinner.getSelectedItemPosition());

        super.onSaveInstanceState(outState);


    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(SAVED_SORT)) {
            int pos = savedInstanceState.getInt(SAVED_SORT);

            if (mSpinner != null)
                mSpinner.setSelection(pos);

            mSpinnerPosition = pos;

        }

    }

}
