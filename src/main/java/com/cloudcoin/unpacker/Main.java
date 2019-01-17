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
        singleRun = isSingleRun(args);
        if (args.length != 0 && Files.exists(Paths.get(args[0]))) {
            System.out.println("New root path: " + args[0]);
            RootPath = args[0];
        }

        FileUtils fileUtils = new FileUtils(RootPath, "Import", "Imported", "Trash", "Suspect");

        System.out.println("found files: " + FileUtils.selectFileNamesInFolder(fileUtils.importFolder).length);

        if (0 != FileUtils.selectFileNamesInFolder(fileUtils.importFolder).length) {
            Unpacker myUnpacker = new Unpacker(fileUtils);
            myUnpacker.importAll();
            exitIfSingleRun();
        }

        FolderWatcher watcher = new FolderWatcher(fileUtils.importFolder);
        System.out.println("Watching folders at " + fileUtils.importFolder + "...");
        while (true) {
            try {
                Thread.sleep(1000);

                // If a change is detected, unpack with Unpacker.
                if (watcher.newFileDetected()) {
                    System.out.println("Unpacking file...");
                    Unpacker myUnpacker = new Unpacker(fileUtils);
                    myUnpacker.importAll();
                    System.out.println("Done unpacking.");
                    exitIfSingleRun();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean singleRun = false;
    public static boolean isSingleRun(String[] args) {
        for (String arg : args)
            if (arg.equals("singleRun"))
                return true;
        return false;
    }
    public static void exitIfSingleRun() {
        if (singleRun)
            System.exit(0);
    }
}
