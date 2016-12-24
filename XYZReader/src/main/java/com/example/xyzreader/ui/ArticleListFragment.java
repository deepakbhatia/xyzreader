package com.example.xyzreader.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.UpdaterService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by obelix on 23/12/2016.
 */

public class ArticleListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ArticleAdapter.AdapterOnClickHandler {

    private static boolean mTwoPane;
    @BindView(R.id.swipe_refresh_layout)
     SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view)
     RecyclerView mRecyclerView;
    private Unbinder unbinder;
    public static Activity activity;
    private ArticleAdapter adapter;
    private boolean mIsRefreshing = false;
    private int mPosition  = RecyclerView.NO_POSITION;



    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    @Override
    public void onClick(int position, long id) {

            ((ArticleSelection)getActivity()).articleSelected(id);
    }

    public interface ArticleSelection{
        public void articleSelected(long articleSelected);
    }

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View articleListView = inflater.inflate(R.layout.list_fragment, container, false);

        unbinder = ButterKnife.bind(this, articleListView);

        if (savedInstanceState == null) {
            refresh();
        }

        adapter = new ArticleAdapter(getActivity(),this);

        activity = getActivity();
        return articleListView;
    }

    private void refresh() {
        getActivity().startService(new Intent(getActivity(), UpdaterService.class));
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(mRefreshingReceiver);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(getActivity());
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {

        int count = cursor.getCount();


        adapter.setCursor(cursor);
        mRecyclerView.setAdapter(adapter);

        if (count > 0) {


            //error.setVisibility(View.GONE);

            if(mTwoPane){

                final long selectedArticleId;

                /*if(adapter.getItemCount() > 0){
                    if (mPosition == RecyclerView.NO_POSITION) {
                        mPosition = 0;
                        selectedArticleId =  adapter.getItemId(mPosition);


                    }else{

                        selectedArticleId =  adapter.getItemId(mPosition);


                    }
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {

                            if(selectedArticleId!=-1)

                                ((ArticleSelection) getActivity()).articleSelected(selectedArticleId);
                        }

                    });
                }
*/
            }

        }
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }




    /*private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private Cursor mCursor;
        private AdapterOnClickHandler clickHandler = null;

        public Adapter(Cursor cursor, AdapterOnClickHandler clickHandler) {
            mCursor = cursor;

            this.clickHandler = clickHandler;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            *//*view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = vh.getAdapterPosition();
                    mCursor.moveToPosition(adapterPosition);
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(vh.getPosition()))));
                }
            });*//*
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

            String byLineText = String.format(activity.getString(R.string.byline_text), DateUtils.getRelativeTimeSpanString(
                    mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString(),mCursor.getString(ArticleLoader.Query.AUTHOR));
            holder.subtitleView.setText( byLineText);

            holder.thumbnailView.setImageUrl(
                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(activity).getImageLoader());
            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }


        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }


        interface AdapterOnClickHandler {
            void onClick(int position);
        }

        public  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
*//*                if(previousSelected!=null)
                {
                    previousSelected.setActivated(false);
                }
                v.setActivated(true);*//*
                ///previousSelected = v;
                clickHandler.onClick(cursor.getString(symbolColumn),adapterPosition);

            }
        }
    }*/
    
    public static void setTwoPane(boolean twoPane){
        
        mTwoPane = twoPane;
    }


}

