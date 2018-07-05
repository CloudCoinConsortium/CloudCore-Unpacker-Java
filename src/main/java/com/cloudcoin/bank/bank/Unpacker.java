package com.cloudcoin.bank.bank;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Unpacker manages the files in the Import folder. The following filetypes will be parsed:
 * <ul>
 * <li>.coin</li>
 * <li>.jpg/jpeg</li>
 * <li>.stack</li>
 * </ul>
 * Files will fist be imported to the {@code Imported} folder. If the file contains valid CloudCoins, they will be
 * extracted to the {@code Suspect} folder. If the file is invalid or duplicated, it will be moved to the {@code
 * Trash} folder. Files in Trash may be renamed if the filename is taken.
 *
 * @author Ben Ward
 * @version 7/5/2018
 */
class Unpacker {

    /** FileUtils for common operations and folder-specific management. */
    private FileUtils fileUtils;


    /**
     * Constructor for objects of class Importer.
     *
     * @param fileUtils The FileUtils object pointing to the CloudCoin folders.
     */
    public Unpacker(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    /**
     * Attempts to import all files in the {@code Import} folder. The following filetypes will be parsed:
     * <ul>
     *     <li>.coin</li>
     *     <li>.jpg/jpeg</li>
     *     <li>.stack</li>
     * </ul>
     * Files will fist be imported to the {@code Imported} folder. If the file contains valid CloudCoins, they will be
     * extracted to the {@code Suspect} folder. If the file is invalid or duplicated, it will be moved to the {@code
     * Trash} folder. Files in Trash may be renamed if the filename is taken.
     */
    public boolean importAll() {
        String[] fileNames = fileUtils.selectFileNamesInFolder(fileUtils.importFolder);
        String extension;

        for (int i = 0; i < fileNames.length; i++) {
            // Unpack every file in the import folder. Move bad files to the Trash folder.
            int indx = fileNames[i].lastIndexOf('.');
            if (indx > 0) {
                extension = fileNames[i].substring(indx + 1);

                if ("stack".equalsIgnoreCase(extension)) {
                    if (importOneFile(fileNames[i]))
                        continue;
                } else if ("coin".equalsIgnoreCase(extension)) {
                    if (importOneFileBinary(fileNames[i]))
                        continue;
                } else if ("jpg".equalsIgnoreCase(extension) ||
                        "jpeg".equalsIgnoreCase(extension)) {
                    if (importOneFileJPEG(fileNames[i]))
                        continue;
                }
            }
        }

        return fileNames.length != 0;
    }

    /** Attempt to import a {@code .stack} CloudCoin file. If unsuccessful, file will be trashed. */
    public boolean importOneFile(String fileNames) {
        if (importStack(fileNames)) {
            fileUtils.moveToImportedFolder(fileNames);
            return true;
        } else {
            fileUtils.moveToTrashFolder(fileNames);
            return false;
        }
    }

    /** Attempt to import a {@code .coin} CloudCoin file. If unsuccessful, file will be trashed. */
    public boolean importOneFileBinary(String fileNames) {
        if (importBinary(fileNames)) {
            fileUtils.moveToImportedFolder(fileNames);
            return true;
        } else {
            fileUtils.moveToTrashFolder(fileNames);
            return false;
        }
    }

    /** Attempt to import a {@code .jpg}/{@code .jpeg} CloudCoin file. If unsuccessful, file will be trashed. */
    public boolean importOneFileJPEG(String fileNames) {
        if (importJPEG(fileNames)) {
            fileUtils.moveToImportedFolder(fileNames);
            return true;
        } else {
            fileUtils.moveToTrashFolder(fileNames);
            return false;
        }
    }

    /** Attempt to read a {@code .coin} CloudCoin file, and extract its CloudCoins. If unsuccessful, file will be trashed. */
    public boolean importBinary(String fileName) {
        try {
            byte[] fileBinary = fileUtils.loadBinaryFromFile(fileName);
            if (0 == fileBinary.length) {
                System.out.println("File " + fileName + " was not found.");
                return false;
            }

            CloudCoin tempCoin = new CloudCoin(fileBinary);
            boolean resultWrite = fileUtils.writeBinaryToReceivedFolder(tempCoin.fileName, tempCoin.binary);
            if (!resultWrite)
                return false;

            fileUtils.moveToImportedFolder(fileName);
            return true;
        } catch (IOException e) {
            System.out.println("File " + fileName + " Corrupt. See CloudCoin file api and edit your file: " + e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("File " + fileName + " was not detected properly." + e);
            e.printStackTrace();
        }
        return false;
    }

    /** Attempt to read a {@code .jpg}/{@code .jpeg} CloudCoin file, and extract its CloudCoins. If unsuccessful, file will be trashed. */
    public boolean importJPEG(String fileName) {
        try {
            CloudCoin tempCoin = fileUtils.cloudCoinFromFile(fileUtils.importFolder + fileName);
            boolean resultWrite = fileUtils.writeTo(fileUtils.suspectFolder, tempCoin);
            if (!resultWrite)
                return false;

            fileUtils.moveToImportedFolder(fileName);
            return true;
        } catch (FileNotFoundException ex) {
            System.out.println("File not found: " + fileName);
        } catch (IOException ioex) {
            System.out.println("IO Exception:" + fileName);
        }
        return false;
    }

    /** Attempt to read a {@code .stack} CloudCoin file, and extract its CloudCoins. If unsuccessful, file will be trashed. */
    public boolean importStack(String fileName) {
        String fileJson;
        fileJson = fileUtils.loadJSON(fileName);
        if (fileJson == null) {
            System.out.println("Error importing stack.");
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
                boolean resultWrite = fileUtils.writeStackToReceivedFolder(tempCoin.fileName, tempCoin.json);
                if (!resultWrite)
                    return false;

                fileUtils.moveToImportedFolder(fileName);
            }
            return true;
        } catch (JSONException ex) {
            System.out.println("Stack File " + fileName + " Corrupt. See CloudCoin file api and edit your stack file: " + ex);
            return false;
        }
    }
}