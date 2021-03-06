package com.cloudcoin.unpacker;

import com.cloudcoin.unpacker.util.CoinUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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


    // Fields

    private FileUtils fileUtils;


    // Constructors

    public Unpacker(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }


    // Methods

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
                        /*|| ("coin".equalsIgnoreCase(extension) && importBinary(fileNames[i]))*/
                        || ("csv".equalsIgnoreCase(extension) && importCsv(fileNames[i]))
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
            boolean resultWrite = fileUtils.writeBinaryToReceivedFolder(tempCoin.currentFilename, fileBinary);
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
            fileUtils.writeCoinToIndividualStacks(tempCoin, fileUtils.suspectFolder);
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

    public boolean importCsv(String filename) {
        ArrayList<String> lines;
        String fullFilePath = fileUtils.importFolder + filename;
        try {
            ArrayList<CloudCoin> csvCoins = new ArrayList<>();
            lines = new ArrayList<>(Files.readAllLines(Paths.get(fullFilePath)));
            for (String line : lines)
                csvCoins.add(CoinUtils.cloudCoinFromCsv(line, fileUtils.importFolder, filename));
            csvCoins.remove(null);
            for (CloudCoin coin : csvCoins) {
                fileUtils.writeCoinToIndividualStacks(coin, fileUtils.suspectFolder);
                fileUtils.moveToImportedFolder(filename);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}