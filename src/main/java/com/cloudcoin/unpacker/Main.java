package com.cloudcoin.unpacker;

import java.io.File;

/**
 * Waits for new files in the import folder, then unpacks them to individual coins.
 *
 * @author Ben Ward
 * @version 7/5/2018
 */
public class Main {

    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println("arg: " + arg);
        }

        FileUtils fileUtils = new FileUtils("C:" + File.separator + "CloudCoinServer" + File.separator +
                "accounts" + File.separator + "DefaultUser" + File.separator,
                "Import", "Imported", "Trash", "Suspect");
        FolderWatcher watcher = new FolderWatcher(fileUtils.importFolder);
        boolean stop = false;

        System.out.println("found files: " + FileUtils.selectFileNamesInFolder(fileUtils.importFolder).length);

        if (0 != FileUtils.selectFileNamesInFolder(fileUtils.importFolder).length) {
            Unpacker myUnpacker = new Unpacker(fileUtils);
            myUnpacker.importAll();
        }

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
