package com.cloudcoin.bank.bank;

/**
 * Waits for new files in the import folder, then unpacks them to individual coins.
 *
 * @author Ben Ward
 * @version 7/5/2018
 */
public class Main {

    public static void main(String[] args) {
        FileUtils fileUtils = new FileUtils("C:\\CloudCoins-Java-Server\\", "Import", "Imported", "Trash", "Suspect");
        FolderWatcher watcher = new FolderWatcher(fileUtils.importFolder);
        boolean stop = false;

        System.out.println("Watching folders at " + fileUtils.importFolder + "...");

        while (!stop) {
            // If a change is detected, unpack with Unpacker.
            if (watcher.newFileDetected()) {
                System.out.println("Unpacking file...");
                Unpacker myUnpacker = new Unpacker(fileUtils);
                myUnpacker.importAll();
                System.out.println("Done unpacking.");
            }
        }
    }
}
