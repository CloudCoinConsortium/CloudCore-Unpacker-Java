package com.cloudcoin.bank.bank.core;

import com.cloudcoin.bank.bank.CloudCoin;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Stack {


    /* Fields */

    @Expose
    @SerializedName("cloudcoin")
    public CloudCoin[] cc;


    /* Methods */

    public Stack(CloudCoin coin) {
        cc = new CloudCoin[1];
        cc[0] = coin;
    }

    public Stack(CloudCoin[] coins) {
        cc = coins;
    }
}