package com.okeefe.peter.movieapp.detailsview;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.LinkAddress;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.format.DateUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import com.okeefe.peter.movieapp.R;
import com.okeefe.peter.movieapp.data.MoviePosterDataContract;
import com.okeefe.peter.movieapp.detailloader.AsyncUpdateFavoritesTask;
import com.okeefe.peter.movieapp.detailloader.Review;
import com.okeefe.peter.movieapp.detailloader.ReviewsLoader;
import com.okeefe.peter.movieapp.detailloader.Trailer;
import com.okeefe.peter.movieapp.detailloader.TrailersLoader;
import com.squareup.picasso.Picasso;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MovieDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Object> {


    private OnFragmentInteractionListener mListener;
    private static final int TRAILERS_LOADER = 1;
    private static final int REVIEWS_LOADER = 0;
    private static final int CURSOR_LOADER = 2;
    public static final String MOVIE_ID_KEY = "movieId";
    public static final String MOVIE_URI_KEY = "movieUri";

    private ArrayAdapter<Trailer> trailerArrayAdapter;
    private Uri mMovieUri;
    private ShareActionProvider mShareActionProvider;
    private Intent mShareIntent;
    private ArrayList<Trailer> trailerArray = new ArrayList<>();

    public MovieDetailsFragment() {

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View out = inflater.inflate(R.layout.fragment_movie_details, container, false);


        trailerArrayAdapter = new TrailerAdapter(getActivity(), R.layout.trailer_item_fragment, trailerArray);


        ListView trailersList = (ListView) out.findViewById(R.id.detail_movie_trailers);

        trailersList.setAdapter(trailerArrayAdapter);


        trailersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trailer t = trailerArrayAdapter.getItem(position);
                t.getKey();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch").buildUpon().appendQueryParameter("v", t.getKey()).build()));
            }
        });
        CheckBox favoriteBox = (CheckBox) out.findViewById(R.id.detail_movie_favorite);

        favoriteBox.setButtonDrawable(android.R.drawable.btn_star);
        favoriteBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                AsyncUpdateFavoritesTask aft = new AsyncUpdateFavoritesTask(getActivity().getContentResolver());
                aft.execute(mMovieUri.toString(), Boolean.toString(isChecked));
                buttonView.setEnabled(false);
            }
        });


        return out;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        String movieId = args.getString(MOVIE_ID_KEY);


        switch (id) {
            case REVIEWS_LOADER:


                return new ReviewsLoader(getActivity(), movieId);


            case TRAILERS_LOADER:
                return new TrailersLoader(getActivity(), movieId);

            case CURSOR_LOADER:

                Uri u = MoviePosterDataContract.CONTENT_URI.buildUpon().appendPath("movies").build();

                return new CursorLoader(getActivity(), u, null, MoviePosterDataContract.MovieEntry.REMOTE_ID + " = ?", new String[]{movieId}, null);

            default:
                throw new IllegalArgumentException("What?");


        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() != null && getActivity() instanceof DetailActivity) {

            Bundle bx = getActivity().getIntent().getExtras();
            Uri u = Uri.parse(bx.getString(MOVIE_URI_KEY));
            setMovieId(u, bx.getString(MOVIE_ID_KEY));

        }
    }

    public void setMovieId(Uri u, String id) {
        this.mMovieUri = u;
        Bundle bx = new Bundle();
        bx.putString(MOVIE_ID_KEY, id);
        if (getActivity().getSupportLoaderManager().getLoader(REVIEWS_LOADER) == null) {


            getActivity().getSupportLoaderManager().initLoader(REVIEWS_LOADER, bx, this);
            getActivity().getSupportLoaderManager().initLoader(TRAILERS_LOADER, bx, this);
            getActivity().getSupportLoaderManager().initLoader(CURSOR_LOADER, bx, this);

        } else {

            getActivity().getSupportLoaderManager().restartLoader(REVIEWS_LOADER, bx, this);
            getActivity().getSupportLoaderManager().restartLoader(TRAILERS_LOADER, bx, this);
            getActivity().getSupportLoaderManager().restartLoader(CURSOR_LOADER, bx, this);

        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {


        inflater.inflate(R.menu.menu_detail, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider)  MenuItemCompat.getActionProvider(item);

        if(mShareActionProvider != null && mShareIntent != null)
            mShareActionProvider.setShareIntent(mShareIntent);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {


        switch (loader.getId()) {

            case REVIEWS_LOADER:


                addReviewsToView((List<Review>) data);


                break;
            case TRAILERS_LOADER:
                if (trailerArrayAdapter != null) {

                    trailerArray.clear();
                    trailerArray.addAll((List<Trailer>) data);
                    trailerArrayAdapter.notifyDataSetChanged();
                    justifyListViewHeightBasedOnChildren((ListView) getView().findViewById(R.id.detail_movie_trailers));





                    Trailer t = trailerArray.get(0);
                    Uri youtubeUri = Uri.parse("http://youtube.com/watch").buildUpon().appendQueryParameter("v", t.getKey() ).build();
                    mShareIntent = new Intent(Intent.ACTION_SEND);
                    mShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                     mShareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_prefix) + " " + youtubeUri.toString());
                     mShareIntent.setType("text/plain");

                    if(mShareActionProvider != null)
                        mShareActionProvider.setShareIntent(mShareIntent);


                }
                break;
            case CURSOR_LOADER:

                Cursor c = (Cursor) data;

                addCursorDataToView(c);


                break;
        }


    }

    private void addCursorDataToView(Cursor c) {
        if (c.moveToFirst()) {
            TextView title = (TextView) getView().findViewById(R.id.detail_movie_title);
            CheckBox favoriteCheck = (CheckBox) getView().findViewById(R.id.detail_movie_favorite);
            TextView releaseDate = (TextView) getView().findViewById(R.id.detail_movie_release_date);
            ImageView poster = (ImageView) getView().findViewById(R.id.detail_movie_poster);
            TextView voteAverage = (TextView) getView().findViewById(R.id.detail_movie_vote_average);
            TextView plot = (TextView) getView().findViewById(R.id.detail_movie_plot_synopsis);


            title.setText(c.getString(c.getColumnIndex(MoviePosterDataContract.MovieEntry.TITLE)));


            voteAverage.setText(Double.toString(c.getDouble(c.getColumnIndex(MoviePosterDataContract.MovieEntry.VOTE_AVERAGE))));
            if (c.getInt(c.getColumnIndex(MoviePosterDataContract.MovieEntry.FAVORITE)) > 0) {
                favoriteCheck.setChecked(true);

            } else {

                favoriteCheck.setChecked(false);
            }

            favoriteCheck.setEnabled(true);


            long rd = c.getLong(c.getColumnIndex(MoviePosterDataContract.MovieEntry.RELEASE_DATE));
            releaseDate.setText(DateUtils.formatDateTime(getActivity(), rd, DateUtils.FORMAT_ABBREV_ALL));
            plot.setText(c.getString(c.getColumnIndex(MoviePosterDataContract.MovieEntry.OVERVIEW)));


            Uri imageUri = Uri.parse("http://image.tmdb.org/t/p").buildUpon().appendPath("w342").appendPath(c.getString(c.getColumnIndex(MoviePosterDataContract.MovieEntry.POSTER_THUMBNAIL)).substring(1)).
                    appendQueryParameter("api_key", getActivity().getString(R.string.api_key_tmdb)).build();


            Picasso.with(getActivity()).load(imageUri).into(poster);


        }
    }

    private void addReviewsToView(List<Review> data) {
        List<Review> reviews = data;

        LinearLayout reviewsTarget = (LinearLayout) getView().findViewById(R.id.review_detail_target);
        reviewsTarget.removeAllViews();
        for (Review r : reviews) {


            TextView tv = new TextView(getContext());
            tv.setSingleLine(false);

            tv.setText(r.getReviewAuthor() + "\n" + r.getReviewContent());
            reviewsTarget.addView(tv);

        }
    }

    public void justifyListViewHeightBasedOnChildren(ListView listView) {

        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            int widthSpec = View.MeasureSpec.makeMeasureSpec(listView.getWidth() - (listView.getPaddingRight() + listView.getPaddingLeft()), View.MeasureSpec.AT_MOST);

            listItem.measure(0, widthSpec);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getPaddingTop() + listView.getPaddingBottom() + listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    @Override
    public void onLoaderReset(Loader loader) {

        switch (loader.getId()) {

            case REVIEWS_LOADER:

                if (getView() != null) {
                    LinearLayout reviewsTarget = (LinearLayout) getView().findViewById(R.id.review_detail_target);
                    if (reviewsTarget != null)
                        reviewsTarget.removeAllViews();
                }
                break;
            case TRAILERS_LOADER:
                if (trailerArrayAdapter != null) {

                    trailerArrayAdapter.clear();

                }
            case CURSOR_LOADER:

                break;

        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }


    private class TrailerAdapter extends ArrayAdapter<Trailer> {

        public TrailerAdapter(Context context, int textViewResourceId, ArrayList<Trailer> list) {
            super(context, textViewResourceId, list);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            View target = convertView;
            if (target == null) {


                LayoutInflater inflater = LayoutInflater.from(getContext());
                target = inflater.inflate(R.layout.trailer_item_fragment, null);
            }
            Trailer trailer = getItem(position);

            if (trailer != null) {

                TextView trailerName = (TextView) target.findViewById(R.id.trailer_name);
                target.setTag(trailer);
                trailerName.setText(trailer.getTrailerName());


            }

            return target;

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }
}
