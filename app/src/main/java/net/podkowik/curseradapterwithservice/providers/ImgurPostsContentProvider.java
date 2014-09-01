package net.podkowik.curseradapterwithservice.providers;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import net.podkowik.curseradapterwithservice.BuildConfig;
import net.podkowik.curseradapterwithservice.model.ImgurPost;

/**
 * Created by christoph.podkowik on 28/08/14.
 */
public class ImgurPostsContentProvider extends android.content.ContentProvider {

    public static final String POSTS_KEY = "POSTS";
    public static DatabaseHelper dbHelper;

    private static final String TAG = "ImgurPostsContentProvider";

    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "content_db";
    public static final String TABLE_NAME = "content_data";

    private static final String CONTENT_AUTHORITY =
            BuildConfig.PACKAGE_NAME + ".providers.ImgurPostsContentProvider";

    private static final Uri BASE_CONTENT_URI =
            Uri.parse("content://" + CONTENT_AUTHORITY);

    private static Uri POSTS_CONTENT_URI;
    public static final int POSTS_CODE = 1;

    private static UriMatcher uriMatcher;


    static {
        POSTS_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(POSTS_KEY).build();
        uriMatcher = buildUriMatcher(CONTENT_AUTHORITY);
    }
    public static UriMatcher buildUriMatcher(String authority) {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(authority, POSTS_KEY, POSTS_CODE);
        return uriMatcher;
    }

    /**
     * Initializes the Database and the UriMatcher
     */
    @Override
    public boolean onCreate() {
        dbHelper = DatabaseHelper.getHelper(getContext());
        return true;
    }

    public static UriMatcher getUriMatcher() {
        return uriMatcher;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor c = null;

        switch (getUriMatcher().match(uri)) {
            case POSTS_CODE: {
                c = getCursorForAllVenues(uri, null, selectionArgs);
            }
            break;
        }
        return c;
    }

    private Cursor getCursorForAllVenues(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + TABLE_NAME + " ORDER BY " +ImgurPost.DATABASE_ID + " ASC ", new String[] {});
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String TAG = "DatabaseHelper";
        private static DatabaseHelper instance;

        public static synchronized DatabaseHelper getHelper(Context context) {
            if (instance == null) {
                instance = new DatabaseHelper(context);
            }
            return instance;
        }

        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sqlTickerTable = "CREATE TABLE " + TABLE_NAME + " ("
                    + ImgurPost.DATABASE_ID + " INTEGER,"
                    + ImgurPost.ID + " VARCHAR(255),"
                    + ImgurPost.TITLE + " VARCHAR(255),"
                    + ImgurPost.TYPE + " VARCHAR(255),"
                    + ImgurPost.ANIMATED + " VARCHAR(255),"
                    + ImgurPost.WIDTH + " INTEGER,"
                    + ImgurPost.HEIGHT + " INTEGER,"
                    + ImgurPost.UPS + " INTEGER,"
                    + ImgurPost.DOWNS + " INTEGER,"
                    + ImgurPost.VIEWS + " INTEGER,"
                    + ImgurPost.SCORE + " INTEGER,"
                    + ImgurPost.DATE + " INTEGER,"
                    + ImgurPost.LINK + " VARCHAR(255),"
                    + ImgurPost.NSFW + " VARCHAR(255),"
                    + "PRIMARY KEY ( "
                    + ImgurPost.DATABASE_ID  + ")" + ");";
            db.execSQL(sqlTickerTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != 0) {
                Log.w(TAG, "Upgrading database from version " + oldVersion
                        + " to " + newVersion
                        + ", which will destroy all old data");
                //Handle migration if needed
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            }
            onCreate(db);
        }
    }

    public static Uri getBaseUri() {
        return BASE_CONTENT_URI;
    }

    public static Uri getPostsUri() {
        return POSTS_CONTENT_URI;
    }


}
