package com.example.xyzreader.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * Created by obelix on 23/12/2016.
 */

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    private ArticleAdapter.AdapterOnClickHandler clickHandler = null;

    public ArticleAdapter(Context context, AdapterOnClickHandler clickHandler) {

        mContext = context;

        this.clickHandler = clickHandler;
    }

    void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }


    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ArticleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_article, parent, false);
        final ArticleAdapter.ViewHolder vh = new ArticleAdapter.ViewHolder(view);
            /*view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = vh.getAdapterPosition();
                    mCursor.moveToPosition(adapterPosition);
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(vh.getPosition()))));
                }
            });*/
        return vh;
    }

    @Override
    public void onBindViewHolder(ArticleAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

        String byLineText = String.format(mContext.getString(R.string.byline_text), DateUtils.getRelativeTimeSpanString(
                mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString(), mCursor.getString(ArticleLoader.Query.AUTHOR));
        holder.subtitleView.setText(byLineText);

        holder.thumbnailView.setImageUrl(
                mCursor.getString(ArticleLoader.Query.THUMB_URL),
                ImageLoaderHelper.getInstance(mContext).getImageLoader());
        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
    }


    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }


    interface AdapterOnClickHandler {
        void onClick(int position, long itemId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);

            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
/*                if(previousSelected!=null)
                {
                    previousSelected.setActivated(false);
                }
                v.setActivated(true);*/
            ///previousSelected = v;
            //TODO
            clickHandler.onClick(adapterPosition,mCursor.getLong(ArticleLoader.Query._ID));

        }
    }
}
