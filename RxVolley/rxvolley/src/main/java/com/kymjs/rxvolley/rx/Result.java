package com.kymjs.rxvolley.rx;

import java.util.Map;

/**
 * @author kymjs (http://www.kymjs.com/) on 12/22/15.
 */
public class Result {
    public Map<String, String> header;
    public byte[] data;

    public Result(Map<String, String> header, byte[] data) {
        this.header = header;
        this.data = data;
    }
}
