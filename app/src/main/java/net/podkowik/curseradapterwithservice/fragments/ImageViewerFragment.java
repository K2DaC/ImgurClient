package net.podkowik.curseradapterwithservice.fragments;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import net.podkowik.curseradapterwithservice.R;
import net.podkowik.curseradapterwithservice.model.ImgurPost;
import net.podkowik.curseradapterwithservice.network.StaticImageCache;
import net.podkowik.curseradapterwithservice.providers.ImgurPostsContentProvider;

/**
 * Created by christoph.podkowik on 29/08/14.
 */
public class ImageViewerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private ImageLoader mImageLoader;
    NetworkImageView mPreviewImage;

    public static ImageViewerFragment newInstance(int position) {
        ImageViewerFragment myFragment = new ImageViewerFragment();
        Bundle args = new Bundle();
        args.putInt(ImgurPostsContentFragment.POSITION, position);
        myFragment.setArguments(args);
        return myFragment;
    }

    public ImageViewerFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        mImageLoader = new ImageLoader(queue, StaticImageCache.getInstance());
        mPreviewImage = (NetworkImageView) rootView.findViewById(R.id.image);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                ImgurPostsContentProvider.getPostsUri(), null, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor c) {
        int position = getArguments().getInt(ImgurPostsContentFragment.POSITION, 0);
        c.moveToPosition(position);
        String link = c.getString(
                c.getColumnIndexOrThrow(
                        ImgurPost.LINK));
        mPreviewImage.setImageUrl(link, mImageLoader);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}
