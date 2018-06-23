package com.cloudcoin.bank.bank.ImportStacks;

import com.cloudcoin.bank.bank.Banker;
import com.cloudcoin.bank.bank.FileUtils;

/**
 * Write a description of class Banker here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class ImportStacks_Banker extends Banker
{
    ImportStacks_FileUtils fileUtils;
    /**
     * Constructor for objects of class Banker
     */
    public ImportStacks_Banker(FileUtils fileUtils )
    {
        super(fileUtils);
        this.fileUtils = (ImportStacks_FileUtils) fileUtils;
    }
}
