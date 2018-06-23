package com.cloudcoin.bank.bank.ImportStacks;

import com.cloudcoin.bank.bank.BankApplication;
import com.cloudcoin.bank.bank.Importer;
import com.cloudcoin.bank.bank.ShowCoins;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ImportCoins {

    @RequestMapping("/import_coins")
    @ResponseBody
    public static void importCoins() {
        System.out.println("Loading all CloudCoins in the import folder:" + BankApplication.importFolder);

        Importer importer = new Importer(BankApplication.fileUtils);
        if (!importer.importAll()) {
            System.out.println("No files were found, ending import.");
            return;
        }

        //Move all coins to seperate JSON files in the the suspect folder.
        Detector detector = new Detector(BankApplication.fileUtils, 10000);
        int[] detectionResults = detector.detectAll();
        System.out.println("Total Received in bank: " + (detectionResults[0] + detectionResults[2]));//And the bank and the fractured for total
        System.out.println("Total Counterfeit: " + detectionResults[1]);

        ShowCoins.showCoins();
    }
}
