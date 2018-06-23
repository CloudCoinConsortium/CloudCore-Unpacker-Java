package com.cloudcoin.bank.bank;

import com.cloudcoin.bank.bank.CloudCoin;
import com.cloudcoin.bank.bank.FileUtils;
import com.cloudcoin.bank.bank.ImportStacks.ImportStacks_FileUtils;
import com.cloudcoin.bank.bank.ImportStacks.ImportStacks_Importer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Dictionary;

/**
 * Everytime you want to import some files into the suspect folder, you create a new importer
 * It pops up and imports the files. It takes jpegs and turns them into JSON. It takes stack files
 * and divides them into individual files. The files will then be ready to be detected.
 *
 * @author Sean H. Worthington
 * @version 1/14/2017
 */
public class Importer {

    FileUtils fileUtils;

    /**
     * Constructor for objects of class Importer
     */
    public Importer(FileUtils fileUtils) {
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
            if (!ImportStacks_Importer.importStack(fileUtils, fname)) {
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
            CloudCoin tempCoin = fileUtils.cloudCoinFromFile(fileUtils.importFolder + fileName);
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


}
