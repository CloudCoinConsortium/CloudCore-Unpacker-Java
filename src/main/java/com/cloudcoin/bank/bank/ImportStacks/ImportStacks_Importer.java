package com.cloudcoin.bank.bank.ImportStacks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * ImportStacks_Importer has the following differences from Importer:
 * Uses ImportStacks_CloudCoin instead of CloudCoin.
 * Uses ImportStacks_FileUtils instead of FileUtils.
 */
class ImportStacks_Importer {

    ImportStacks_FileUtils fileUtils;

    /**
     * Constructor for objects of class Importer
     */
    public ImportStacks_Importer(ImportStacks_FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    public boolean importAll() {
        String[] fnames = fileUtils.selectFileNamesInFolder(fileUtils.importFolder);
        System.out.println("Importing the following files (" + fnames.length + "):");

        for (int i = 0; i < fnames.length; i++) {
            System.out.println(fnames[i]);

            if (!importOneFile(fnames[i])) {
                fileUtils.moveToTrashFolder(fnames[i]);
            }
        }
        if (fnames.length == 0) {
            System.out.println("There were not CloudCoins to import. Please place our CloudCoin .jpg and .stack files in your imports folder at " + fileUtils.importFolder);
            return false;
        } else {
            return true;
        }
    }

    public boolean importOneFile(String fname) {
        String extension = "";
        int index = fname.lastIndexOf('.');
        if (index > 0) {
            extension = fname.substring(index + 1);
        }
        extension = extension.toLowerCase();
        if (extension.equals("jpeg") || extension.equals("jpg")) {
            if (!importJPEG(fname)) {
                fileUtils.moveToTrashFolder(fname);
                return false;// System.out.println("Failed to load JPEG file");
            }
        } else if (extension.equals("stack")) {
            if (!importStack(fname)) {
                fileUtils.moveToTrashFolder(fname);
                return false;// System.out.println("Failed to load .stack file");
            }
        }
        //change imported file to have a .imported extention
        fileUtils.writeToSuspectFolder(fname);
        fileUtils.moveToImportedFolder(fname);
        return true;
    }

    public boolean importJPEG(String fileName) {
        try {
            ImportStacks_CloudCoin tempCoin = fileUtils.cloudCoinFromFile(fileUtils.importFolder + fileName);
            fileUtils.writeTo(fileUtils.suspectFolder, tempCoin);
            //System.out.println("File saved to " + importFolder + fileName);
            fileUtils.moveToImportedFolder(fileName);
            return true;
        } catch (FileNotFoundException ex) {
            System.out.println("File not found: " + fileName);
        } catch (IOException ioex) {
            System.out.println("IO Exception:" + fileName);
        }
        return false;
    }

    public boolean importStack(String fileName) {
        String fileJson;
        try {
            fileJson = fileUtils.importJSON(fileName);
        } catch (IOException ex) {
            System.out.println("Error importing stack " + ex);
            return false;
        }
        JSONArray incomeJsonArray;
        try {
            JSONObject json = new JSONObject(fileJson);
            incomeJsonArray = json.getJSONArray("cloudcoin");
            ImportStacks_CloudCoin tempCoin;
            for (int i = 0; i < incomeJsonArray.length(); i++) {
                JSONObject childJSONObject = incomeJsonArray.getJSONObject(i);
                int nn = childJSONObject.getInt("nn");
                int sn = childJSONObject.getInt("sn");
                JSONArray an = childJSONObject.getJSONArray("an");
                String[] ans = ImportStacks_FileUtils.toStringArray(an);
                String ed = childJSONObject.getString("ed");

                tempCoin = new ImportStacks_CloudCoin(nn, sn, ans, ed, null, "suspect");
                fileUtils.writeTo(fileUtils.suspectFolder, tempCoin);
                fileUtils.moveToImportedFolder(fileName);
            }
            return true;
        } catch (JSONException ex) {
            System.out.println("Stack File " + fileName + " Corrupt. See CloudCoin file api and edit your stack file: " + ex);
            return false;
        }
    }
}
