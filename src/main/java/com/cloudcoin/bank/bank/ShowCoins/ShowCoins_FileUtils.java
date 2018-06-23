package com.cloudcoin.bank.bank.ShowCoins;

import java.io.*;
import java.util.*;

/**
 * Help to read, write and change files.
 *
 * ShowCoins_FileUtils has the following differences from FileUtils:
 * Uses ShowCoins_CloudCoin instead of CloudCoin.
 * Removed all methods except selectFileNamesInFolder().
 *
 * @author Sean H. Worthington
 * @version 1/17/2017
 */
class ShowCoins_FileUtils
{
    // instance variables
    public  String rootFolder;
    public  String importFolder;
    public  String importedFolder;
    public  String trashFolder;
    public  String suspectFolder;
    public  String frackedFolder;
    public  String bankFolder;
    public  String templateFolder;
    public  String counterfeitFolder;
    public  String directoryFolder;
    public  String exportFolder;

    /**
     * Constructor for objects of class FileUtils
     */
    public ShowCoins_FileUtils(String rootFolder, String importFolder, String importedFolder, String trashFolder, String suspectFolder, String frackedFolder, String bankFolder, String templateFolder, String counterfeitFolder, String directoryFolder, String exportFolder)
    {
        // initialise instance variables
        this.rootFolder = rootFolder ;
        this.importFolder = importFolder;
        this.importedFolder = importedFolder;
        this.trashFolder = trashFolder;
        this.suspectFolder = suspectFolder;
        this.frackedFolder = frackedFolder;
        this.bankFolder = bankFolder;
        this.templateFolder = templateFolder;
        this.counterfeitFolder = counterfeitFolder;
        this.directoryFolder = directoryFolder;
        this.exportFolder = exportFolder;
    }//End constructor

    /***
     * Given directory path return an array of strings of all the files in the directory.
     * @parameter directoryPath The location of the directory to be scanned
     * @return filenames The names of all the files in the directory
     */
    public String[] selectFileNamesInFolder(String directoryPath) {
        File dir = new File(directoryPath);
        String candidateFileExt = "";
        Collection<String> files  =new ArrayList<String>();
        if(dir.isDirectory()){
            File[] listFiles = dir.listFiles();

            for(File file : listFiles){
                if(file.isFile()) {//Only add files with the matching file extension
                    files.add(file.getName());
                }
            }
        }
        return files.toArray(new String[]{});
    }//End select all file names in a folder

}
