package com.cloudcoin.bank.bank.ImportStacks;

import com.cloudcoin.bank.bank.CloudCoin;
import com.cloudcoin.bank.bank.RAIDA;

/**
 * Redundant Array of Independent Detection Agents
 * This operates all 25 Detection Agents as a group. 
 * 
 * @author Sean H. Worthington
 * @version 1/8/2016
 */
public class ImportStacks_RAIDA extends RAIDA {


    /**
     * RAIDA Constructor
     *
     * @param milliSecondsToTimeOut The number of milliseconds that requests shall be allowed to run before timing out.
     */
    public ImportStacks_RAIDA(int milliSecondsToTimeOut )
    {
        super(milliSecondsToTimeOut);
    }
}
