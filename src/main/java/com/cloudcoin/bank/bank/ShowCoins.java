package com.cloudcoin.bank.bank;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public abstract class ShowCoins {

    @RequestMapping("/showCoins")
    @ResponseBody
    public static void showCoins() {
        Banker bank = new Banker(BankApplication.fileUtils);
        int[] bankTotals = bank.countCoins(BankApplication.bankFolder);
        int[] frackedTotals = bank.countCoins(BankApplication.frackedFolder);
        //int[] counterfeitTotals = bank.countCoins( counterfeitFolder );
        int grandTotal = bankTotals[0] + frackedTotals[0];

        System.out.println("Your Bank Inventory:");
        System.out.println("Total: " + grandTotal);
        System.out.print("  1s: " + (bankTotals[1] + frackedTotals[1]) + " || ");
        System.out.print("  5s: " + (bankTotals[2] + frackedTotals[2]) + " ||");
        System.out.print(" 25s: " + (bankTotals[3] + frackedTotals[3]) + " ||");
        System.out.print("100s: " + (bankTotals[4] + frackedTotals[4]) + " ||");
        System.out.println("250s: " + (bankTotals[5] + frackedTotals[5]));
    }
}
