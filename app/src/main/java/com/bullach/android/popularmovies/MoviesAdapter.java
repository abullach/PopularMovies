package com.bullach.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bullach.android.popularmovies.model.Movie;
import com.bullach.android.popularmovies.utils.QueryUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {

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

    // Provides a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView titleTextView;
        public ImageView posterImageView;

        // Creates a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            titleTextView = (TextView) itemView.findViewById(R.id.tvMovieTitle);
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

            if (adapterPosition != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                Movie movie = mMovies.get(adapterPosition);
                mClickHandler.onClick(movie);
            }
        }
    }

    @Override
    public MoviesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.movie_list_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MoviesAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Movie movie = mMovies.get(position);

        Uri uri;
        uri = QueryUtils.buildPosterImageUrl(movie.getMoviePosterPath(), QueryUtils.POSTER_IMAGE_SIZE_W185_PATH);

        Picasso.with(mContext)
                .load(uri)
                .placeholder(R.drawable.ic_poster_placeholder_original_black_24dp)
                .error(R.drawable.ic_error_outline_black_24dp)
                .into(viewHolder.posterImageView);

        TextView textView = viewHolder.titleTextView;
        textView.setText(movie.getMovieTitle());
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        if (null == mMovies) return 0;
        return mMovies.size();
    }

    // Clear all elements of the recycler
    public void clear() {
        mMovies.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Movie> movies) {
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }
}
