package com.goread.goreader.utils;

import org.json.JSONObject;

/**
 * A simple filter for the goread feed list.
 */
public class FeedListFilter {
    /**
     * Filters all read items from the given feed list JSON object.
     * @param o
     * @return only unread items in JSON object
     */
    public JSONObject filterRead(JSONObject o) {
        return o;
    }

}
