package com.cloudcoin.bank.bank.ImportStacks;

import com.cloudcoin.bank.bank.Base.FileUtils;

import java.io.File;

class Application_ImportStacks {
    public static String rootFolder = "C:\\CloudCoins-Java-Server" + File.separator ;
    public static String importFolder = rootFolder + "Import" + File.separator;
    public static String importedFolder = rootFolder +  "Imported" + File.separator;
    public static String trashFolder = rootFolder +  "Trash" + File.separator;
    public static String suspectFolder = rootFolder +  "Suspect" + File.separator;
    public static String frackedFolder = rootFolder +  "Fracked" + File.separator;
    public static String bankFolder = rootFolder +  "Bank" + File.separator;
    public static String templateFolder = rootFolder +  "Templates" + File.separator;
    public static String counterfeitFolder = rootFolder +  "Counterfeit" + File.separator;
    public static String directoryFolder = rootFolder +  "Directory" + File.separator;;
    public static String exportFolder = rootFolder +  "Export" + File.separator;

    public static FileUtils baseFileUtils = new FileUtils( rootFolder, importFolder, importedFolder, trashFolder, suspectFolder, frackedFolder, bankFolder, templateFolder, counterfeitFolder, directoryFolder, exportFolder  );
    public static ImportStacks_FileUtils fileUtils = new ImportStacks_FileUtils( rootFolder, importFolder, importedFolder, trashFolder, suspectFolder, frackedFolder, bankFolder, templateFolder, counterfeitFolder, directoryFolder, exportFolder  );

    public static void main(String[] args) {
        ImportStacks_FolderWatcher watcher = new ImportStacks_FolderWatcher("C:\\CloudCoins-Java-Server\\Import");

        // Currently runs endlessly.
        boolean stop = false;
        while (!stop) {
            if (watcher.newFileDetected()) {
                ImportStacks.importCoins();
            }
        }
    }
}
