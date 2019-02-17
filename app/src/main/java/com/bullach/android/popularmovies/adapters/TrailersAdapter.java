package com.bullach.android.popularmovies.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bullach.android.popularmovies.R;
import com.bullach.android.popularmovies.models.Trailer;

import java.util.List;

public class TrailersAdapter  extends RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder> {

    private List<Trailer> mTrailer;
    private final TrailersOnClickListener mDetailsClickListener;
    private Context context;

    public interface TrailersOnClickListener{
        void trailerOnClick(Trailer trailer);
    }

    public TrailersAdapter(TrailersOnClickListener onClickListener, Context context) {
        mDetailsClickListener = onClickListener;
        this.context = context;
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mTrailerTextView;

        public TrailerViewHolder(View itemView) {
            super(itemView);

            mTrailerTextView = itemView.findViewById(R.id.tvTrailer);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Trailer trailer = mTrailer.get(adapterPosition);
                mDetailsClickListener.trailerOnClick(trailer);
            }
        }
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerViewHolder trailerViewHolder, int position) {

        Trailer trailer = mTrailer.get(position);
        TextView textView = trailerViewHolder.mTrailerTextView;
        textView.setText(trailer.getName());
    }

    @Override
    public int getItemCount() {

        if (null == mTrailer) return 0;
        return mTrailer.size();
    }

    public void setTrailersData(List<Trailer> trailersData) {
        mTrailer = trailersData;
        notifyDataSetChanged();
    }
}
