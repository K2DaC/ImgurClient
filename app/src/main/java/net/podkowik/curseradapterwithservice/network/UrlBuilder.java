package net.podkowik.curseradapterwithservice.network;

import org.apache.http.HttpException;

/**
 * Created by christoph.podkowik on 28/08/14.
 */
public class UrlBuilder {
    public static final String TAG = "UrlBuilder";

    public static final String IMGUR_ROOT_URL = "https://api.imgur.com/3";
    public static final String IMGUR_GALLERY = "/gallery";
    public static final String IMGUR_HOT = "/hot";
    public static final String IMGUR_VIRAL = "/viral";
    public static final String IMGUR_TIME = "/time";
    public static final String IMGUR_RESPONSE_TPYE = ".json";
    public static final String CLIENT_ID = "Client-ID 129487aa6af7586"; // TODO REMOVE

    private final StringBuilder mBuilder = new StringBuilder();
    private String mRootUrl;

    public UrlBuilder(String rootUrl) {
        mRootUrl = rootUrl;
    }

    private UrlBuilder reset() {
        mBuilder.setLength(0);
        return this;
    }

    public UrlBuilder param(String param) {
        mBuilder.append(param).append("&");
        return this;
    }

    public UrlBuilder param(String key, String value) {
        mBuilder.append(key).append("=").append(value).append("&");
        return this;
    }

    public UrlBuilder param(String key, int value) {
        mBuilder.append(key).append("=").append(value).append("&");
        return this;
    }

    public UrlBuilder restUrlPart(String part) {
        mBuilder.append(part);
        return this;
    }

    private void assertRootUrl() {
        if (mRootUrl == null) {
            throw new IllegalStateException("URL not specified");
        }
    }

    public String getUrl() {
        return mBuilder.toString();
    }

    public String buildUrl()
            throws HttpException {
        assertRootUrl();
        mBuilder.insert(0, mRootUrl);
        final String url = mBuilder.toString();
        reset();
        return url;
    }
}