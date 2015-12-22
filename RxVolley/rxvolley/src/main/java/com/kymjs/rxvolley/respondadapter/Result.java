package com.kymjs.rxvolley.respondadapter;

import java.util.Map;

/**
 * @author kymjs (http://www.kymjs.com/) on 12/22/15.
 */
public class Result {
    public Map<String, String> header;
    public byte[] data;

    public Result(Map<String, String> header, byte[] result) {
        this.header = header;
        this.data = result;
    }
}
