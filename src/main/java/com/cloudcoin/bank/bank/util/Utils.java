package com.cloudcoin.bank.bank.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {


    public static Gson createGson() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
    }
}
