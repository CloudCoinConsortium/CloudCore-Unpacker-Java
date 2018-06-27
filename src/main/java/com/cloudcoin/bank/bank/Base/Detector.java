package com.cloudcoin.bank.bank.Base;

import java.io.IOException;

/**
 * Reads files from the suspect folder, detects them.
 * Writes them to either the bank, counterfeit or fracked
 *
 * @author Sean H. Worthington
 * @version 1/14/2017
 */
public class Detector {

    RAIDA raida;
    FileUtils fileUtils;

    public Detector(FileUtils fileUtils, int timeout) {
        raida = new RAIDA(timeout);
        this.fileUtils = fileUtils;
    }

    public int[] detectAll() {
        int[] results = new int[3];//[0] Coins to bank, [1] Coins to fracked [1] Coins to Counterfeit
        int totalValueToBank = 0;
        int totalValueToCounterfeit = 0;
        int totalValueToFractured = 0;
        CloudCoin newCC;
        String[] suspectFileNames = fileUtils.selectFileNamesInFolder(fileUtils.suspectFolder);

        for (int j = 0; j < suspectFileNames.length; j++) {
            try {
                newCC = fileUtils.cloudCoinFromFile(fileUtils.suspectFolder + suspectFileNames[j]);
                System.out.println("");
                System.out.println("");
                System.out.println("Detecting SN #" + newCC.sn + ", Denomination: " + newCC.getDenomination());
                CloudCoin detectedCC = raida.detectCoin(newCC);

                detectedCC.consoleReport();
                switch (detectedCC.extension) {
                    case "bank":
                        totalValueToBank++;
                        fileUtils.writeTo(fileUtils.bankFolder, detectedCC);
                        break;
                    case "fractured":
                        totalValueToFractured++;
                        fileUtils.writeTo(fileUtils.frackedFolder, detectedCC);
                        break;
                    case "counterfeit":
                        totalValueToCounterfeit++;
                        fileUtils.writeTo(fileUtils.counterfeitFolder, detectedCC);
                        break;
                }

                fileUtils.deleteCoin(fileUtils.suspectFolder + suspectFileNames[j]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //System.out.println("Results of Import:");
        results[0] = totalValueToBank;
        results[1] = totalValueToCounterfeit; //System.out.println("Counterfeit and Moved to trash: "+totalValueToCounterfeit);
        results[2] = totalValueToFractured;//System.out.println("Fracked and Moved to Fracked: "+ totalValueToFractured);
        return results;
    }
}
