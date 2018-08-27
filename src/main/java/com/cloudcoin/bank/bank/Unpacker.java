package com.cloudcoin.bank.bank;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

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
        String[] fileNames = FileUtils.selectFileNamesInFolder(fileUtils.importFolder);
        String extension;

        // Unpack every file in the import folder. Move bad files to the Trash folder.
        for (int i = 0; i < fileNames.length; i++) {
            System.out.println("Importing file " + fileNames[i]);
            int index = fileNames[i].lastIndexOf('.');
            if (index > 0) {
                extension = fileNames[i].substring(index + 1);

                if (("stack".equalsIgnoreCase(extension) && importStack(fileNames[i]))
                    || ("coin".equalsIgnoreCase(extension) && importBinary(fileNames[i]))
                    || (("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) && importJPEG(fileNames[i])))
                    fileUtils.moveToImportedFolder(fileNames[i]);
                else
                    fileUtils.moveToTrashFolder(fileNames[i]);
            }
        }

        return fileNames.length != 0;
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
        } catch (Exception e) {
            System.out.println("File " + fileName + " was not imported.");
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
        } catch (IOException ex) {
            System.out.println("File " + fileName + " was not imported.");
            ex.printStackTrace();
        }
        return false;
    }

    /** Attempt to read a {@code .stack} CloudCoin file, and extract its CloudCoins. If unsuccessful, file will be trashed. */
    public boolean importStack(String fileName) {
        ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromStack(fileUtils.importFolder + fileName);
        for (CloudCoin coin : coins) {
            fileUtils.writeCoinToIndividualStacks(coin, fileUtils.suspectFolder);
            fileUtils.moveToImportedFolder(fileName);
        }
        return true;
    }
}