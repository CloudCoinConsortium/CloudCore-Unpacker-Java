package com.cloudcoin.bank.bank.ShowCoins;

/**
 * ShowCoins_ShowCoins has the following differences from ShowCoins:
 * Uses Application_ShowCoins instead of BankApplication.
 */
public abstract class ShowCoins_ShowCoins {

    public static void showCoins() {
        ShowCoins_Banker bank = new ShowCoins_Banker(Application_ShowCoins.fileUtils);
        int[] bankTotals = bank.countCoins(Application_ShowCoins.bankFolder);
        int[] frackedTotals = bank.countCoins(Application_ShowCoins.frackedFolder);
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
