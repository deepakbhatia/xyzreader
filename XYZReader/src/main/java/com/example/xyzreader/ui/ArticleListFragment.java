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
    private int mPosition, mSelectedItemPosition ;
    private static final String SELECTED_ITEM = "SELECTED_ITEM";



    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };
    private String SCROLLED_ITEM = "SCROLLED_ITEM";

    @Override
    public void onClick(int position, long id) {

        mSelectedItemPosition  = position;
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
        setRetainInstance(true);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState!=null && savedInstanceState.containsKey(SELECTED_ITEM)){
            mSelectedItemPosition = savedInstanceState.getInt(SELECTED_ITEM,RecyclerView.NO_POSITION);
        }

        getLoaderManager().initLoader(0, null, this);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
    RecyclerViewPositionHelper mRecyclerViewHelper;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View articleListView = inflater.inflate(R.layout.list_fragment, container, false);

        unbinder = ButterKnife.bind(this, articleListView);

        if (savedInstanceState == null) {
            refresh();
        }

        adapter = new ArticleAdapter(getActivity(),this);

        mRecyclerView.addItemDecoration(new android.support.v7.widget.DividerItemDecoration(getActivity(),1));


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mRecyclerViewHelper = RecyclerViewPositionHelper.createHelper(recyclerView);

                int firstVisibleItem = mRecyclerViewHelper.findFirstVisibleItemPosition();

                mPosition = mRecyclerViewHelper.findFirstCompletelyVisibleItemPosition();


            }
        });

        activity = getActivity();
        return articleListView;
    }

    private void refresh() {
        getActivity().startService(new Intent(getActivity(), UpdaterService.class));
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_ITEM,mSelectedItemPosition);

        outState.putInt(SCROLLED_ITEM,mPosition);


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

                if(adapter.getItemCount() > 0){

                    if (mPosition == RecyclerView.NO_POSITION) {
                        mPosition = 0;
                        if(mSelectedItemPosition == RecyclerView.NO_POSITION)
                            mSelectedItemPosition = 0;
                        selectedArticleId =  adapter.getItemId(mSelectedItemPosition);


                    }else{

                        if(mSelectedItemPosition == RecyclerView.NO_POSITION)
                            mSelectedItemPosition = mPosition;
                        selectedArticleId =  adapter.getItemId(mSelectedItemPosition);


                    }
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {

                            if(selectedArticleId!=-1)

                                ((ArticleSelection) getActivity()).articleSelected(selectedArticleId);
                        }

                    });
                }

            }



        }
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
        if (mPosition == RecyclerView.NO_POSITION)
            mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);



    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        if(mRecyclerView!=null)
        mRecyclerView.setAdapter(null);
    }





    
    public static void setTwoPane(boolean twoPane){
        
        mTwoPane = twoPane;
    }


}

