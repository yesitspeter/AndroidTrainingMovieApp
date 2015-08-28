package com.okeefe.peter.movieapp.detailloader;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.okeefe.peter.movieapp.R;
import com.okeefe.peter.movieapp.sync.MovieContentSyncAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by peterokeefe on 8/27/15.
 */
public class ReviewsLoader extends AsyncTaskLoader<List<Review>> {


    private static final String LOG_KEY = "ReviewsLoader";
    private String mRemoteId;
    private List<Review> mReviews;

    @Override
    public List<Review> loadInBackground() {

        try {
            LinkedList<Review> out = new LinkedList<>();
            JSONArray reviewsArray = getReviewsArray();

            for (int i = 0; i < reviewsArray.length(); ++i) {

                JSONObject review = reviewsArray.getJSONObject(i);

                Review rx = new Review();
                rx.setReviewAuthor(review.getString("author"));
                rx.setReviewContent(review.getString("content"));
                rx.setReviewUrl(review.getString("url"));
                rx.setReviewId(review.getString("id"));

                out.add(rx);
                Log.i(LOG_KEY, "Received Review");

            }
            mReviews = out;
            return out;
        } catch (Exception e) {
            Log.wtf(LOG_KEY, e);

        }
        return null;
    }

    public ReviewsLoader(Context context, String movieRemoteId) {
        super(context);
        mRemoteId = movieRemoteId;

    }

    @Override
    protected void onStartLoading() {
        if (mReviews != null) {
            deliverResult(mReviews);
        } else {
            forceLoad();
        }
    }

    public JSONArray getReviewsArray() {

        Context context = getContext();

        try {
            Uri.Builder base = Uri.parse("http://api.themoviedb.org/3/movie").buildUpon();
            base.appendPath(mRemoteId);
            base.appendPath("reviews");

            base.appendQueryParameter("api_key", context.getString(R.string.api_key_tmdb));
            base.appendQueryParameter("language", "en");
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

                String jsonString = MovieContentSyncAdapter.readString(in);
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
}
