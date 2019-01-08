package me.saket.dank.ui.user;

import net.dean.jraw.databind.UnixTime;
import com.squareup.moshi.ToJson;
import com.squareup.moshi.FromJson;


public class MoshiUnixTimeAdapter {
    @ToJson
    public String toJson(Object value) {
        return value.toString();
    }

    @FromJson
    public String fromJson(UnixTime value) {
        return value.toString();
    }
}
