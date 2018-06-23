package com.cloudcoin.bank.bank.ImportStacks;


class ImportStacks {

    public static void importCoins() {
        System.out.println("Loading all CloudCoins in the import folder:" + Application_ImportStacks.importFolder);

        ImportStacks_Importer importer = new ImportStacks_Importer(Application_ImportStacks.fileUtils);
        if (!importer.importAll()) {
            System.out.println("No files were found, ending import.");
            return;
        }

        // Move all coins to seperate JSON files in the the suspect folder.
        ImportStacks_Detector detector = new ImportStacks_Detector(Application_ImportStacks.fileUtils, 10000);
        int[] detectionResults = detector.detectAll();
        System.out.println("Total Received in bank: " + (detectionResults[0] + detectionResults[2]));//And the bank and the fractured for total
        System.out.println("Total Counterfeit: " + detectionResults[1]);

        ImportStacks_ShowCoins.showCoins();
    }
}
