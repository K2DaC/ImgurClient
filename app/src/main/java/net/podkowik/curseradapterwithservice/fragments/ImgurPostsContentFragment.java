package net.podkowik.curseradapterwithservice.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import net.podkowik.curseradapterwithservice.ImageViewerActivity;
import net.podkowik.curseradapterwithservice.R;
import net.podkowik.curseradapterwithservice.adapters.ImgurPostsCursorAdapter;
import net.podkowik.curseradapterwithservice.model.ImgurPost;
import net.podkowik.curseradapterwithservice.providers.ImgurPostsContentProvider;
import net.podkowik.curseradapterwithservice.services.ImgurPostsDownloadService;

/**
 * Created by christoph.podkowik on 28/08/14.
 */
public class ImgurPostsContentFragment extends android.support.v4.app.Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    public static String POSITION = "position";
    public static String SIZE = "size";

    private GridView mGridView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mLoadingPagination;
    private ImgurPostsCursorAdapter mAdapter;
    private int mPage = 0;

    public ImgurPostsContentFragment() {
    }

    public static ImgurPostsContentFragment getInstance() {
        return new ImgurPostsContentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridView);
        mGridView.setOnItemClickListener(this);

        mGridView.setOnScrollListener( new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView view,
                                 int firstVisibleItem,
                                 int visibleItemCount,
                                 final int totalItemCount)
            {
                if (totalItemCount > 0)
                {
                    int lastInScreen = firstVisibleItem + visibleItemCount;
                    /*
                    * If the last item in the gridView is visible, load more content!
                    * This is called multiple times, so make sure we only load once each time
                    * */
                    if (lastInScreen == totalItemCount && !mLoadingPagination)
                    {
                       mPage++;
                       mLoadingPagination = true;
                       mSwipeRefreshLayout.setRefreshing(true);
                       Intent intent = new Intent(getActivity(), ImgurPostsDownloadService.class);
                       intent.putExtra(ImgurPostsDownloadService.PAGE, mPage);
                       intent.putExtra(ImgurPostsDownloadService.TASK_TYPE,
                               ImgurPostsDownloadService.TASK_PAGINATE_CONTENT
                       );
                       getActivity().startService(intent);
                    }
                }
            }
        });

        mSwipeRefreshLayout =
                (SwipeRefreshLayout) rootView.findViewById(R.id.brochureGridPullToRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.blue,
                R.color.green,R.color.purple);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ImgurPostsDownloadService.PAGE, mPage);
        super.onSaveInstanceState(outState);
    }

    private void reload() {
        Intent intent = new Intent(getActivity(), ImgurPostsDownloadService.class);
        intent.putExtra(ImgurPostsDownloadService.TASK_TYPE,
                ImgurPostsDownloadService.TASK_FORCE_DOWNLOAD_CONTENT
        );
        getActivity().startService(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mPage = savedInstanceState.getInt(ImgurPostsDownloadService.PAGE);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter = new ImgurPostsCursorAdapter(getActivity(), null, true);
        mGridView.setAdapter(mAdapter);
        Intent intent = new Intent(getActivity(), ImgurPostsDownloadService.class);
        intent.putExtra(ImgurPostsDownloadService.TASK_TYPE,
                ImgurPostsDownloadService.TASK_FORCE_DOWNLOAD_CONTENT
        );
        intent.putExtra(ImgurPostsDownloadService.PAGE,
                0
        );
        getActivity().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        setViewVisibility(ViewState.LOADING);
        if (mSwipeRefreshLayout.getVisibility() == View.VISIBLE){
            mSwipeRefreshLayout.setRefreshing(true);
        }
        return new CursorLoader(getActivity(),
                ImgurPostsContentProvider.getPostsUri(), null, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (mAdapter == null) {
            mAdapter = new ImgurPostsCursorAdapter(getActivity(), cursor, false);
            mGridView.setAdapter(mAdapter);
        }
        if (cursor != null && cursor.getCount() == 0) {
            mAdapter.notifyDataSetChanged();
            setViewVisibility(ViewState.EMPTY);
        } else {
            if (cursor != null) {
                mAdapter.swapCursor(cursor);
                setViewVisibility(ViewState.LIST);
            } else {
                mAdapter.notifyDataSetChanged();
                setViewVisibility(ViewState.EMPTY);
            }
        }
        mLoadingPagination = false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGridView.setAdapter(null);
    }

    private void setViewVisibility(ViewState viewState) {
        View view = getView();
        switch (viewState) {
            case LIST:
                //view.findViewById(R.id.listLoadingView).setVisibility(View.GONE);
                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                break;
            case LOADING:
                //view.findViewById(R.id.listLoadingView).setVisibility(View.VISIBLE);
                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                //view.findViewById(R.id.listLoadingView).setVisibility(View.GONE);
                mSwipeRefreshLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
        Cursor c = ((ImgurPostsCursorAdapter) mGridView.getAdapter()).getCursor();
        c.moveToPosition(i);
        String link = c.getString(
                c.getColumnIndexOrThrow(
                        ImgurPost.LINK));
        intent.putExtra(ImgurPost.LINK, link);
        intent.putExtra(POSITION, i);
        intent.putExtra(SIZE, c.getCount());
        startActivity(intent);
    }

    private enum ViewState {
        LIST,
        LOADING,
        EMPTY
    }
}