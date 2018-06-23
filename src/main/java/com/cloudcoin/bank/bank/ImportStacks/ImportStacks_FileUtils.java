package com.cloudcoin.bank.bank.ImportStacks;

import com.cloudcoin.bank.bank.CloudCoin;
import com.cloudcoin.bank.bank.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ImportStacks_FileUtils extends FileUtils {

    /**
     * Constructor for objects of class FileUtils
     *
     * @param rootFolder
     * @param importFolder
     * @param importedFolder
     * @param trashFolder
     * @param suspectFolder
     * @param frackedFolder
     * @param bankFolder
     * @param templateFolder
     * @param counterfeitFolder
     * @param directoryFolder
     * @param exportFolder
     */
    public ImportStacks_FileUtils(String rootFolder, String importFolder, String importedFolder, String trashFolder, String suspectFolder, String frackedFolder, String bankFolder, String templateFolder, String counterfeitFolder, String directoryFolder, String exportFolder) {
        super(rootFolder, importFolder, importedFolder, trashFolder, suspectFolder, frackedFolder, bankFolder, templateFolder, counterfeitFolder, directoryFolder, exportFolder);
    }
}
