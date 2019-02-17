package com.bullach.android.popularmovies.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bullach.android.popularmovies.R;
import com.bullach.android.popularmovies.models.Movie;
import com.bullach.android.popularmovies.utils.QueryUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder> {

    private List<Movie> mMovies;
    private Context mContext;

    /**
     * Creates a MoviesAdapter.
     * @param context Context
     * @param movies List of movies
     * @param clickHandler The on-click handler for this adapter. This single handler is called when an item is clicked.
     */
    public MoviesAdapter(Context context, List<Movie> movies, MoviesAdapterOnClickHandler clickHandler) {
        mContext = context;
        mMovies = movies;
        mClickHandler = clickHandler;
    }

    /**
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final MoviesAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface MoviesAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    public class MoviesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView posterImageView;

        public MoviesViewHolder(View itemView) {
            super(itemView);
            posterImageView = (ImageView) itemView.findViewById(R.id.ivPosterImage);
            itemView.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            if (adapterPosition != RecyclerView.NO_POSITION) {
                Movie movie = mMovies.get(adapterPosition);
                mClickHandler.onClick(movie);
            }
        }
    }

    @Override
    public MoviesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.movie_list_item, parent, false);
        return new MoviesViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesViewHolder viewHolder, int position) {
        // Get the data model based on position
        Movie movie = mMovies.get(position);

        Uri uri;
        uri = QueryUtils.buildPosterImageUrl(movie.getMoviePosterPath(), QueryUtils.POSTER_IMAGE_SIZE_W185_PATH);

        Picasso.with(mContext)
                .load(uri)
                .placeholder(R.drawable.ic_poster_placeholder_original_black_24dp)
                .error(R.drawable.ic_error_outline_black_24dp)
                .into(viewHolder.posterImageView);
    }

    @Override
    public int getItemCount() {
        if (null == mMovies) return 0;
        return mMovies.size();
    }

    public void clear() {
        mMovies.clear();
        notifyDataSetChanged();
    }

    public void setMoviesData(List<Movie> moviesData) {
        mMovies = moviesData;
        notifyDataSetChanged();
    }
}
