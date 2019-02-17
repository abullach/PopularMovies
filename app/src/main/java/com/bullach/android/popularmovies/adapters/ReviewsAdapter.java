package com.bullach.android.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bullach.android.popularmovies.R;
import com.bullach.android.popularmovies.models.Review;

import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>{

    private List<Review> mReview;
    private Context context;

    public ReviewsAdapter(Context context) {
        this.context = context;
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        public TextView mAuthorNameTextView;
        public TextView mReviewTextView;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            mAuthorNameTextView = itemView.findViewById(R.id.tvAuthorName);
            mReviewTextView  = itemView.findViewById(R.id.tvReviewContent);
        }
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.review_list_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder reviewViewHolder, int position) {
        Review review = mReview.get(position);
        TextView author = reviewViewHolder.mAuthorNameTextView;
        author.setText(review.getAuthor());

        TextView reviewText = reviewViewHolder.mReviewTextView;
        reviewText.setText(review.getReview());
    }

    @Override
    public int getItemCount() {
        if (null == mReview) return 0;
        return mReview.size();
    }

    public void setReviewsAdapter(List<Review> reviewsData) {
        mReview = reviewsData;
        notifyDataSetChanged();
    }
}
