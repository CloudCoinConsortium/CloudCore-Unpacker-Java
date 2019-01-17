package com.cloudcoin.unpacker;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Waits for new files in the import folder, then unpacks them to individual coins.
 *
 * @author Ben Ward
 * @version 7/5/2018
 */
public class Main {

    public static String RootPath = "C:\\Users\\Public\\Documents\\CloudCoin\\";

    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println("arg: " + arg);
        }
        if (args.length != 0 && Files.exists(Paths.get(args[0]))) {
            System.out.println("New root path: " + args[0]);
            RootPath = args[0];
        }

        FileUtils fileUtils = new FileUtils(RootPath, "Import", "Imported", "Trash", "Suspect");
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

        while (!stop) {
            try {
                Thread.sleep(1000);

                // If a change is detected, unpack with Unpacker.
                if (watcher.newFileDetected()) {
                    System.out.println("Unpacking file...");
                    Unpacker myUnpacker = new Unpacker(fileUtils);
                    myUnpacker.importAll();
                    System.out.println("Done unpacking.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
