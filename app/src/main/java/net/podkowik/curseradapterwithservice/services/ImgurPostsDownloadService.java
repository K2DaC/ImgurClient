package net.podkowik.curseradapterwithservice.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.podkowik.curseradapterwithservice.model.ImgurPost;
import net.podkowik.curseradapterwithservice.network.UrlBuilder;
import net.podkowik.curseradapterwithservice.network.WebHelper;
import net.podkowik.curseradapterwithservice.providers.ImgurPostsContentProvider;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christoph.podkowik on 28/08/14.
 */
public class ImgurPostsDownloadService extends IntentService {

    private static final String TAG = "ContentDownloadService";
    public static String TASK_TYPE = "de.kaufda.android.task_type";
    public static String PAGE = "page";
    private static String JSON_DATA_KEY = "data";
    public static final int TASK_REFRESH_CONTENT_IF_NESSESSERY = 0x1;
    public static final int TASK_FORCE_DOWNLOAD_CONTENT = 0x2;
    public static final int TASK_PAGINATE_CONTENT = 0x3;
    private static final long MILLISECS_TILL_REFRESH = 5 * 60 * 1000; // 5 Min
    private static long sLastRefresh;

    public ImgurPostsDownloadService() {
        super(ImgurPostsDownloadService.class.getSimpleName());
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        int taskType = intent.getIntExtra(TASK_TYPE, 0);
        switch (taskType) {
            case TASK_REFRESH_CONTENT_IF_NESSESSERY: {
                if (System.currentTimeMillis() > (sLastRefresh + MILLISECS_TILL_REFRESH)) {
                    downloadContent();
                }
            }
            break;
            case TASK_FORCE_DOWNLOAD_CONTENT: {
                downloadContent();
            }
            break;
            case TASK_PAGINATE_CONTENT: {
                int page = intent.getIntExtra(PAGE, 0);
                loadPage(page);
            }
            break;
            default: {
                downloadContent();
            }
            break;
        }
    }

    private void downloadContent() {
        sLastRefresh = System.currentTimeMillis();
        loadPage(0);
    }

    private void loadPage(int page) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.IMGUR_ROOT_URL);
        String url;
        JSONObject jObject = null;
        try {
            url = builder
                    .restUrlPart(UrlBuilder.IMGUR_GALLERY)
                    .restUrlPart(UrlBuilder.IMGUR_HOT)
                    .restUrlPart(UrlBuilder.IMGUR_TIME)
                    .restUrlPart("/" + page)
                    .restUrlPart(UrlBuilder.IMGUR_RESPONSE_TPYE)
                    .buildUrl();
            jObject = WebHelper.getHttpJson(url);
        } catch (HttpException e) {
            e.printStackTrace();
        }
        if (jObject != null) {
            insertVenues(jObject, page);
        }
    }

    private List<ContentValues> cursorToContentValues(Cursor cursor) {
        ArrayList<ContentValues> list = new ArrayList<ContentValues>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            String title = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.TITLE));
            String score = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.SCORE));
            String link = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.LINK));
            String id = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.ID));
            String type = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.TYPE));
            String animated = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.ANIMATED));
            String nsfw = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.NSFW));
            int date = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.DATE));
            int ups = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.UPS));
            int downs = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.DOWNS));
            int widht = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.WIDTH));
            int height = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.HEIGHT));
            int views = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            ImgurPost.VIEWS));

            ContentValues values = new ContentValues();
            values.put(ImgurPost.VIEWS, views);
            values.put(ImgurPost.VIEWS, views);
            values.put(ImgurPost.HEIGHT, height);
            values.put(ImgurPost.WIDTH, widht);
            values.put(ImgurPost.DOWNS, downs);
            values.put(ImgurPost.UPS, ups);
            values.put(ImgurPost.DATE, date);
            values.put(ImgurPost.NSFW, nsfw);
            values.put(ImgurPost.ANIMATED, animated);
            values.put(ImgurPost.TYPE, type);
            values.put(ImgurPost.ID, id);
            values.put(ImgurPost.LINK, link);
            values.put(ImgurPost.SCORE, score);
            values.put(ImgurPost.TITLE, title);
            list.add(values);
            cursor.moveToNext();
        }
        return list;
    }

    private void insertVenues(JSONObject jObject, int page) {

        SQLiteDatabase db = ImgurPostsContentProvider.dbHelper.getWritableDatabase();
        if (db == null) {
            return;
        }

        List<ContentValues> list = null;
        /*
        * If the user uses Pull To Refresh
        * we just add the content to the end of the database.
        * ASC makes sure, it will be on top.
        *
        * If the user uses pagination, we need to somehow add the content to the front of the db, therefor
        * - we backup the DB
        * - drop the table (clear the DB)
        * - add everything we loaded
        * - remove duplicated entries from out backup, as the new once are more up to date
        * - add the backup to the database
        * */
        if (page == 0) {
            Cursor c = db.rawQuery("select * from " +
                    ImgurPostsContentProvider.TABLE_NAME + " ORDER BY "
                    +ImgurPost.DATABASE_ID + " ASC ",
                    new String[] {});
            list = cursorToContentValues(c);
            db.delete(ImgurPostsContentProvider.TABLE_NAME, null, null);
        }
        db.beginTransaction();
        try {
            if (jObject.has(JSON_DATA_KEY)) {
                    JSONArray jArray = jObject.getJSONArray(JSON_DATA_KEY);
                    ContentValues values;
                    for (int i = 0; i < jArray.length(); i++) {
                        values = new ContentValues();
                        JSONObject jPost = jArray.getJSONObject(i);
                        values.put(ImgurPost.ID, jPost.getString(ImgurPost.ID));
                        removeFromList(list, jPost.getString(ImgurPost.ID));
                        if (jPost.has(ImgurPost.TITLE)) {
                            values.put(ImgurPost.TITLE, jPost.getString(ImgurPost.TITLE));
                        } else {
                            values.put(ImgurPost.TITLE, "-");
                        }

                        if (jPost.has(ImgurPost.TYPE)) {
                            values.put(ImgurPost.TYPE, jPost.getString(ImgurPost.TYPE));
                        } else {
                            values.put(ImgurPost.TYPE, "-");
                        }

                        if (jPost.has(ImgurPost.ANIMATED)) {
                            values.put(ImgurPost.ANIMATED, jPost.getString(ImgurPost.ANIMATED));
                        } else {
                            values.put(ImgurPost.ANIMATED, "-");
                        }

                        if (jPost.has(ImgurPost.WIDTH)) {
                            values.put(ImgurPost.WIDTH, jPost.getInt(ImgurPost.WIDTH));
                        } else {
                            values.put(ImgurPost.WIDTH, 0);
                        }

                        if (jPost.has(ImgurPost.HEIGHT)) {
                            values.put(ImgurPost.HEIGHT, jPost.getInt(ImgurPost.HEIGHT));
                        } else {
                            values.put(ImgurPost.HEIGHT, 0);
                        }

                        if (jPost.has(ImgurPost.VIEWS)) {
                            values.put(ImgurPost.VIEWS, jPost.getInt(ImgurPost.VIEWS));
                        } else {
                            values.put(ImgurPost.VIEWS, 0);
                        }

                        if (jPost.has(ImgurPost.UPS)) {
                            values.put(ImgurPost.UPS, jPost.getInt(ImgurPost.UPS));
                        } else {
                            values.put(ImgurPost.UPS, 0);
                        }

                        if (jPost.has(ImgurPost.DOWNS)) {
                            values.put(ImgurPost.DOWNS, jPost.getInt(ImgurPost.DOWNS));
                        } else {
                            values.put(ImgurPost.DOWNS, 0);
                        }

                        if (jPost.has(ImgurPost.SCORE)) {
                            values.put(ImgurPost.SCORE, jPost.getInt(ImgurPost.SCORE));
                        } else {
                            values.put(ImgurPost.SCORE, 0);
                        }

                        if (jPost.has(ImgurPost.LINK)) {
                            values.put(ImgurPost.LINK, jPost.getString(ImgurPost.LINK));
                        } else {
                            values.put(ImgurPost.LINK, "-");
                        }

                        if (jPost.has(ImgurPost.NSFW)) {
                            values.put(ImgurPost.NSFW, jPost.getString(ImgurPost.NSFW));
                        } else {
                            values.put(ImgurPost.NSFW, "-");
                        }
                        if (jPost.has(ImgurPost.DATE)) {
                            values.put(ImgurPost.DATE, jPost.getInt(ImgurPost.DATE));
                        } else {
                            values.put(ImgurPost.DATE, 1);
                        }
                        int count = db
                                .update(ImgurPostsContentProvider.TABLE_NAME,
                                        values,
                                        ImgurPost.ID
                                                + "='"
                                                + jPost.getString(ImgurPost.ID) + "'",
                                        null);
                        // we want to display only images, which are not type:gif
                        if (jPost.has(ImgurPost.TYPE) && jPost.getString(ImgurPost.TYPE).contains("image")
                                && !jPost.getString(ImgurPost.TYPE).contains("gif") && count == 0) {
                            db.insert(ImgurPostsContentProvider.TABLE_NAME,
                                ImgurPost.DATABASE_ID, values);
                        }

                    }

            }
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    db.insert(ImgurPostsContentProvider.TABLE_NAME,
                            ImgurPost.DATABASE_ID, list.get(i));
                }

            }
            db.setTransactionSuccessful();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
           db.endTransaction();
        }
        getContentResolver().notifyChange(
                ImgurPostsContentProvider.getBaseUri(), null);
    }

    private void removeFromList(List<ContentValues> list, String string) {
        if (list != null){
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getAsString(ImgurPost.ID).equals(string)){
                    list.remove(i);
                    break;
                }
            }
        }
    }
}
