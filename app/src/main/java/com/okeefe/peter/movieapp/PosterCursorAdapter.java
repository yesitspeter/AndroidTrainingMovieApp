package com.okeefe.peter.movieapp;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.okeefe.peter.movieapp.data.MoviePosterDataContract;
import com.okeefe.peter.movieapp.data.MoviePosterDbHelper;
import com.squareup.picasso.Picasso;

/**
 * Created by peterokeefe on 8/26/15.
 */
public class PosterCursorAdapter extends CursorAdapter {


    public PosterCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public PosterCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {


        View vx = LayoutInflater.from(context).inflate(R.layout.fragment_posteritem, parent, false);


        return vx;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {


        int favorite = cursor.getInt(cursor.getColumnIndex(MoviePosterDataContract.MovieEntry.FAVORITE));
        Uri imageUri = Uri.parse("http://image.tmdb.org/t/p").buildUpon().appendPath("w342").appendPath(cursor.getString(cursor.getColumnIndex(MoviePosterDataContract.MovieEntry.POSTER_THUMBNAIL)).substring(1)).
                appendQueryParameter("api_key", context.getString(R.string.api_key_tmdb)).build();

        if (view instanceof ImageView)
            Picasso.with(context).load(imageUri).into((ImageView) view);
        else {
            View vx = view.findViewById(R.id.posteritem_poster_image);
            Picasso.with(context).load(imageUri).into((ImageView) vx);

            ImageView posterMarker = (ImageView) view.findViewById(R.id.poster_item_favorite_mark);
            if (favorite == 0) {
                posterMarker.setVisibility(View.INVISIBLE);
            } else {
                posterMarker.setVisibility(View.VISIBLE);


            }
        }


    }
}
