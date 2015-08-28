package com.okeefe.peter.movieapp;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.okeefe.peter.movieapp.data.MoviePosterDataContract;
import com.okeefe.peter.movieapp.data.MoviePosterDbHelper;
import com.okeefe.peter.movieapp.sync.MovieContentSyncAdapter;
import com.okeefe.peter.movieapp.sync.SyncService;

/**
 * A placeholder fragment containing a simple view.
 */
public class PostersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final String SAVED_SELECTION = "savedSelection";
    private static final String GRID_STATE = "savedState";
    private static final String SORT_BY_KEY = "sortBy";


    private static String LOG_KEY = "PostersFragment";
    private PosterCursorAdapter mPosterAdapter;
    private GridView mGridView;
    private int mPosition = ListView.INVALID_POSITION;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mSortBy = MoviePosterDataContract.SORT_ORDER_POPULARITY;
    private Parcelable mGridViewState = null;
    private boolean mFavoritesOnly = false;
    private boolean firstOpen = true;


    public PostersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        mPosterAdapter = new PosterCursorAdapter(getActivity(), null, 0);


        View out = inflater.inflate(R.layout.fragment_movie_posters, container, false);


        mSwipeRefreshLayout = (SwipeRefreshLayout) out.findViewById(R.id.activity_main_swipe_refresh_layout);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                forceUpdate();


            }
        });

        mGridView = (GridView) out.findViewById(R.id.posters_poster_gridview);
        mGridView.setAdapter(mPosterAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cx = (Cursor) parent.getItemAtPosition(position);

                if (cx != null) {
                    long movieId = cx.getLong(cx.getColumnIndex(MoviePosterDataContract.MovieEntry._ID));
                    String remoteId = cx.getString(cx.getColumnIndex(MoviePosterDataContract.MovieEntry.REMOTE_ID));

                    ((PosterSelectedCallback) getActivity()).onItemSelected(MoviePosterDataContract.CONTENT_URI.buildUpon().appendPath("movies").appendPath(Long.toString(movieId)).build(), remoteId);

                    mPosition = position;
                }


            }
        });
        Log.i(LOG_KEY, "onCreateView finished");

        return out;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(SORT_BY_KEY)) {

            mSortBy = savedInstanceState.getString(SORT_BY_KEY);

        }

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_SELECTION)) {
            mPosition = savedInstanceState.getInt(SAVED_SELECTION);
            mGridView.setSelection(mPosition);
            Log.i(LOG_KEY, "Restored position on onviewcreated");

        }
        if (savedInstanceState != null && savedInstanceState.containsKey(GRID_STATE))
            mGridViewState = savedInstanceState.getParcelable(GRID_STATE);

        if (mGridViewState != null) {
            mGridView.onRestoreInstanceState(mGridViewState);
            Log.i(LOG_KEY, "Restored state on onviewcreated");

        }
        Log.i(LOG_KEY, "onViewCreated");

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by date.
        String sortOrder = mSortBy;

        Uri u = MoviePosterDataContract.CONTENT_URI.buildUpon().appendPath("movies").build();


        String selection = null;
        String[] selectionParams = null;

        if (mFavoritesOnly) {

            selection = MoviePosterDataContract.MovieEntry.FAVORITE + " = ?";
            selectionParams = new String[]{"1"};
        }


        return new CursorLoader(getActivity(),
                u,
                null,
                selection,
                selectionParams,
                sortOrder);
    }

    public void setSortOrderAndFavoritesOnly(boolean favoritesOnly, String sortBy) {


        Log.i(LOG_KEY, "Setting sort order and favorites: " + favoritesOnly + " " + sortBy);
        mGridViewState = mGridView.onSaveInstanceState();

        mFavoritesOnly = favoritesOnly;
        if (sortBy != null)
            mSortBy = sortBy;

        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPosterAdapter.swapCursor(data);


        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);

        if (data.getCount() == 0 && firstOpen) {
            firstOpen = false;
            forceUpdate();
        }
        firstOpen = false;


        if (mPosition != ListView.INVALID_POSITION) {
            Log.i(LOG_KEY, "Restored position after load");

            mGridView.setSelection(mPosition);
        }
        Log.i(LOG_KEY, "Finished Loading");

        if (mGridViewState != null) {
            mGridView.onRestoreInstanceState(mGridViewState);
            mGridViewState = null;

            Log.i(LOG_KEY, "Restored state after load");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_KEY, "Resumed");

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPosterAdapter.swapCursor(null);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);


        getLoaderManager().initLoader(0, null, this);


    }

    private void forceUpdate() {
        MovieContentSyncAdapter.syncImmediately(getActivity());
        getLoaderManager().restartLoader(0, null, this).forceLoad();


    }

    public void onSetSortOrder() {

        getLoaderManager().restartLoader(0, null, this);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SAVED_SELECTION, mPosition);
        if (mGridView != null)
            outState.putParcelable(GRID_STATE, mGridView.onSaveInstanceState());
        Log.i(LOG_KEY, "Saving state in saveinstancestate");

        outState.putString(SORT_BY_KEY, mSortBy);

    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface PosterSelectedCallback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri movieUri, String remoteId);
    }


}
