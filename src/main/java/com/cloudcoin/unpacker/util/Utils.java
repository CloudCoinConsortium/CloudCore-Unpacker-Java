package com.cloudcoin.unpacker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {


    /* Methods */

    public static Gson createGson() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
    }
}
