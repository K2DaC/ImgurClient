package net.podkowik.curseradapterwithservice.network;

import android.util.Log;

import net.podkowik.curseradapterwithservice.BuildConfig;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Created by christoph.podkowik on 28/08/14.
 */
public class WebHelper {

    private static String TAG = "WebHelper";;

    public static JSONObject getHttpJson(String url) throws HttpException {
        JSONObject json = null;
        HttpGet httpGet = new HttpGet(url.trim());
        httpGet.addHeader("Authorization", UrlBuilder.CLIENT_ID);
        String result = executeHttp(httpGet);
        if (result == null)
            return json;

        try {
            json = new JSONObject(result);
        } catch (JSONException e) {
            Log.e(TAG, HttpErrorMessages.JSON_PARSING_ERROR, e);
        }

        if (json == null) {
            throw new HttpException(HttpErrorMessages.JSON_ERROR);
        }
        return json;
    }

    public static String executeHttp(HttpUriRequest request)
            throws HttpException {
        String result = null;
        InputStream instream = null;
        try {
            request.addHeader("Accept-Encoding", "gzip");
            HttpClient httpClient = new DefaultHttpClient();
            final HttpResponse response = httpClient.execute(request);
            final int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                throw new HttpException(HttpErrorMessages.UNEXPECTED_SERVER_RESPONSE + " "
                        + response.getStatusLine() + " for " + request.getURI());
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, response.getStatusLine() + " : " + request.getURI());
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                final Header contentEncoding = response
                        .getFirstHeader("Content-Encoding");
                instream = entity.getContent();
                if (contentEncoding != null
                        && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                    instream = new GZIPInputStream(instream);
                }
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(instream));
                StringBuilder sb = new StringBuilder();
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        instream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                result = sb.toString();
            }
        } catch (ClientProtocolException e) {
            throw new HttpException(HttpErrorMessages.PROTOCOL_ERROR, e);
        } catch (IOException e) {
            throw new HttpException(HttpErrorMessages.IO_ERROR, e);
        } catch (IllegalArgumentException e) {
            throw new HttpException(HttpErrorMessages.INVALID_URI, e);
        } finally {
            try {
                if (instream != null)
                    instream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
