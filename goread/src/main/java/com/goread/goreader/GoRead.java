package com.goread.goreader;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.NoCache;
import com.jakewharton.disklrucache.DiskLruCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public final class GoRead {
    public static final String TAG = "goread";
    public static final int PICK_ACCOUNT_REQUEST = 1;
    public static final String APP_ENGINE_SCOPE = "ah";
    public static final String P_ACCOUNT = "ACCOUNT_NAME";

    public JSONObject lj = null;
    public JSONObject stories = null;
    public HashMap<String, JSONObject> feeds;
    public DiskLruCache storyCache = null;
    private RequestQueue rq = null;
    public UnreadCounts unread = null;
    private HashMap<String, String> icons = new HashMap<String, String>();

    boolean loginDone = false;
    File feedCache = null;

    private static final GoRead INSTANCE = new GoRead();

    private GoRead() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static GoRead get() {
        return INSTANCE;
    }

    public static String getIcon(String f) {
        return get().icons.get(f);
    }

    public static void updateFeedProperties() {
        get().doUpdateFeedProperties();
    }

    public static String hashStory(JSONObject j) throws JSONException {
        return hashStory(j.getString("Feed"), j.getString("Story"));
    }

    public static String hashStory(String feed, String story) {
        MessageDigest cript = null;
        try {
            cript = MessageDigest.getInstance("SHA-1");
            cript.reset();
            cript.update(feed.getBytes("utf8"));
            cript.update("|".getBytes());
            cript.update(story.getBytes());
        } catch (NoSuchAlgorithmException e) {
            Log.e(GoRead.TAG, e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            Log.e(GoRead.TAG, e.getMessage(), e);
        }
        String sha = new BigInteger(1, cript.digest()).toString(16);
        return sha;
    }

    private void doUpdateFeedProperties() {
        final String suffix = "=s16";
        try {
            Log.e(TAG, "ufp");
            stories = lj.getJSONObject("Stories");
            unread = new UnreadCounts();
            JSONArray opml = lj.getJSONArray("Opml");
            updateFeedProperties(null, opml);
            HashMap<String, String> ic = new HashMap<String, String>();
            opml = lj.getJSONArray("Feeds");
            for (int i = 0; i < opml.length(); i++) {
                JSONObject o = opml.getJSONObject(i);
                String im = o.getString("Image");
                if (im.length() == 0) {
                    continue;
                }
                if (im.endsWith(suffix)) {
                    im = im.substring(0, im.length() - suffix.length());
                }
                ic.put(o.getString("Url"), im);
            }
            icons = ic;
        } catch (JSONException e) {
            Log.e(TAG, "ufp", e);
        }
    }

    private void updateFeedProperties(String folder, JSONArray opml) {
        try {
            for (int i = 0; i < opml.length(); i++) {
                JSONObject outline = opml.getJSONObject(i);
                if (outline.has("Outline")) {
                    updateFeedProperties(outline.getString("Title"), outline.getJSONArray("Outline"));
                } else {
                    String f = outline.getString("XmlUrl");
                    if (!stories.has(f)) {
                        continue;
                    }
                    JSONArray us = stories.getJSONArray(f);
                    Integer c = 0;
                    for (int j = 0; j < us.length(); j++) {
                        if (!us.getJSONObject(j).optBoolean("read", false)) {
                            c++;
                        }
                    }
                    if (c == 0) {
                        continue;
                    }
                    unread.All += c;
                    if (!unread.Feeds.containsKey(f)) {
                        unread.Feeds.put(f, 0);
                    }
                    unread.Feeds.put(f, unread.Feeds.get(f) + c);
                    if (folder != null) {
                        if (!unread.Folders.containsKey(folder)) {
                            unread.Folders.put(folder, 0);
                        }
                        unread.Folders.put(folder, unread.Folders.get(folder) + c);
                    }
                }
            }
            persistFeedList();
        } catch (JSONException e) {
            Log.e(TAG, "ufp2", e);
        }
    }

    private void persistFeedList() {
        try {
            FileWriter fw = new FileWriter(feedCache);
            fw.write(lj.toString());
            fw.close();
            Log.e(TAG, "write feed cache");
        } catch (IOException e) {
            Log.e(GoRead.TAG, e.getMessage(), e);
        }
    }

    public static void addReq(Request r) {
        GoRead g = get();
        if (g.rq == null) {
            g.rq = new RequestQueue(new NoCache(), new BasicNetwork(new OkHttpStack()));
            g.rq.start();
        }
        g.rq.add(r);
    }
}
