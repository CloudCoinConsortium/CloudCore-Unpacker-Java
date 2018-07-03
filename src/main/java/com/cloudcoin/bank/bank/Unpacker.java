package com.cloudcoin.bank.bank;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * ImportStacks_Importer has the following differences from Importer:
 * Uses ImportStacks_CloudCoin instead of CloudCoin.
 * Uses ImportStacks_FileUtils instead of FileUtils.
 */
class Unpacker {

    /**
     * Fields
     */
    FileUtils fileUtils;

    
    /**
     * Constructor for objects of class Importer
     */
    public Unpacker( FileUtils fileUtils) {
       this.fileUtils = fileUtils;
    }

    public boolean importAll() {
        String[] fileNames = fileUtils.selectFileNamesInFolder( fileUtils.importFolder );
        String extension ="";
        //System.out.println("Importing the following files (" + fnames.length + "):");
        for (int i = 0; i < fileNames.length; i++) {
            //System.out.println(fnames[i]);
             //Go through every file in the import folder. If unpack fails, trash that file. 
             
             /*SEE IF FILE IS JPEG OR JSON*/
             int indx = fileNames[i].lastIndexOf('.');
             if (indx > 0) {
                 extension = fileNames[i].substring(indx+1);
                 if( extension.equalsIgnoreCase("stack") || extension.equalsIgnoreCase("coin")){
                           if (!importOneFile(fileNames[i])) {//Failedt trying to import one file
                               fileUtils.moveToTrashFolder(fileNames[i]);
                            }// end if failed trying to import one file
                 }
             }
        }
        if (fileNames.length == 0) {
            //System.out.println("There were no CloudCoins to import. Please place our CloudCoin .jpg and .stack files in your imports folder at " + fileUtils.importFolder);
            return false;
        } else {
            return true;
        }
    }

    public boolean importOneFile(String fileNames) {
        if (importStack(fileNames)) {//Upack successful
            fileUtils.moveToImportedFolder(fileNames);
        }
        else//Failed to unpack
        {
            fileUtils.moveToTrashFolder(fileNames);
            return false;// System.out.println("Failed to load .stack file");
        }//End if
        return true;
    }

    public boolean importOneFileBinary(String fileNames) {
        if (importBinary(fileNames)) {//Upack successful
            fileUtils.moveToImportedFolder(fileNames);
        }
        else//Failed to unpack
        {
            fileUtils.moveToTrashFolder(fileNames);
            return false;// System.out.println("Failed to load .stack file");
        }//End if
        return true;
    }

    public boolean importBinary(String fileName) {
        try {
            byte[] fileBinary = fileUtils.loadBinaryFromFile(fileName);
            CloudCoin tempCoin = new CloudCoin(fileBinary);
            fileUtils.writeStackToReceivedFolder(tempCoin.fileName, tempCoin.json);
            fileUtils.moveToImportedFolder(fileName);
        } catch (IOException e) {
            System.out.println("File " + fileName + " Corrupt. See CloudCoin file api and edit your file: " + e);
            e.printStackTrace();
        }
        return false;
    }

    public boolean importStack(String fileName) {
        String fileJson;
        try {
            fileJson = fileUtils.loadJSON(fileName);
        } catch (IOException ex) {
            System.out.println("Error importing stack " + ex);
            return false;
        }
        JSONArray incomeJsonArray;
        try {
            JSONObject json = new JSONObject(fileJson);
            incomeJsonArray = json.getJSONArray("cloudcoin");
            CloudCoin tempCoin;
            for (int i = 0; i < incomeJsonArray.length(); i++) {
                JSONObject childJSONObject = incomeJsonArray.getJSONObject(i);
                int nn = childJSONObject.getInt("nn");
                int sn = childJSONObject.getInt("sn");
                JSONArray an = childJSONObject.getJSONArray("an");
                String[] ans = FileUtils.toStringArray(an);
                String ed = childJSONObject.getString("ed");

                tempCoin = new CloudCoin(nn, sn, ans);
                fileUtils.writeStackToReceivedFolder(tempCoin.fileName, tempCoin.json);
                fileUtils.moveToImportedFolder(fileName);
            }
            return true;
        } catch (JSONException ex) {
            System.out.println("Stack File " + fileName + " Corrupt. See CloudCoin file api and edit your stack file: " + ex);
            return false;
        }
    }
    

}