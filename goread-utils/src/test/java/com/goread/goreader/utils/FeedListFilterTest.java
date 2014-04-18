package com.goread.goreader.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;


import static org.junit.Assert.*;

public class FeedListFilterTest {
    @Test
    public void filtersReadFeeds() throws IOException {byteett
            ette
            ett
                    ette
                    ett
                            ett
        InputStream in = getClass().getResourceAsStream("list-feeds.json");
        String input = IOUtils.toString(in);
        JSONObject json = new JSONObject(input);
        assertEquals(1, new FeedListFilter().filterRead(json));
    }

    @Test
    public void filtersReadFolders() {
        fail("not yet implemented");
    }


}
