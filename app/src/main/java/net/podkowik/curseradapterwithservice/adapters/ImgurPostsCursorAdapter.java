package net.podkowik.curseradapterwithservice.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import net.podkowik.curseradapterwithservice.R;
import net.podkowik.curseradapterwithservice.model.ImgurPost;
import net.podkowik.curseradapterwithservice.network.StaticImageCache;

/**
 * Created by christoph.podkowik on 28/08/14.
 */
public class ImgurPostsCursorAdapter extends CursorAdapter {

    private static final String TAG = "ImgurPostsCursorAdapter";

    private ImageLoader mImageLoader;
    private Context mContext;
    private LayoutInflater mInflater;

    public ImgurPostsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mContext = context;
        RequestQueue queue = Volley.newRequestQueue(mContext);
        mImageLoader = new ImageLoader(queue, StaticImageCache.getInstance());
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final View view = mInflater.inflate(R.layout.single_list_layout, null);
        ViewHolder holder  =   new ViewHolder();
        holder.ups = (TextView) view.findViewById(R.id.upVotes);
        holder.downs = (TextView) view.findViewById(R.id.downVotes);
        holder.previewImage = (NetworkImageView) view.findViewById(R.id.image);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String url = cursor.getString(
                cursor.getColumnIndexOrThrow(
                        ImgurPost.LINK));

        int ups = cursor.getInt(
                cursor.getColumnIndexOrThrow(
                        ImgurPost.UPS));

        int downs = cursor.getInt(
                cursor.getColumnIndexOrThrow(
                        ImgurPost.DOWNS));

        String lowResolutionUrl = transformToLowResolutionUrl(url);
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.downs.setText(String.valueOf(downs));
        holder.ups.setText(String.valueOf(ups));
        holder.previewImage.setDefaultImageResId(R.drawable.placeholder);
        holder.previewImage.setImageUrl(lowResolutionUrl, mImageLoader);
    }

    /**
     * This method adds a "m" infront of the filetype in the url
     * This is how the API is designed.
     * e.g.
     * http://url/myImage.jpg
     * will be changed into
     * http://url/myImagem.jpg
     *
     * @param url
     *      The url which should be adjusted
     * @return
     *      The new url with the "m" included
     * */
    private String transformToLowResolutionUrl(String url) {
        int endIndex = url.lastIndexOf(".");
        String firstPart = url.substring(0,endIndex);
        String lastPart = url.substring(endIndex, url.length());
        return firstPart + ImgurPost.MEDIUM_IMAGE_SUFFIX + lastPart;
    }

    /**
     * ViewHolder Class for ImgurPostsCursorAdapter
     * */
    private static class ViewHolder  {
        TextView ups;
        TextView downs;
        NetworkImageView previewImage;
    }
}
