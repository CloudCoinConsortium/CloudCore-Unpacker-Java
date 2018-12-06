package com.cloudcoin.unpacker.util;

import com.cloudcoin.unpacker.CloudCoin;
import com.cloudcoin.unpacker.core.Config;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public class CoinUtils {

    /**
     * Returns a denomination describing the currency value of the CloudCoin.
     *
     * @param coin CloudCoin
     * @return 1, 5, 25, 100, 250, or 0 if the CloudCoin's serial number is invalid.
     */
    public static int getDenomination(CloudCoin coin) {
        int sn = coin.getSn();
        int nom;
        if (sn < 1)
            nom = 0;
        else if ((sn < 2097153))
            nom = 1;
        else if ((sn < 4194305))
            nom = 5;
        else if ((sn < 6291457))
            nom = 25;
        else if ((sn < 14680065))
            nom = 100;
        else if ((sn < 16777217))
            nom = 250;
        else
            nom = 0;

        return nom;
    }

    /**
     * Generates a name for the CloudCoin based on the denomination, Network Number, and Serial Number.
     * <br>
     * <br>Example: 25.1.6123456
     *
     * @return String a filename
     */
    public static String generateFilename(CloudCoin coin) {
        return getDenomination(coin) + ".CloudCoin." + coin.getNn() + "." + coin.getSn();
    }

    /**
     * Generates secure random GUIDs for pans. An example:
     * <ul>
     * <li>8d3eb063937164c789474f2a82c146d3</li>
     * </ul>
     * These Strings are hexadecimal and have a length of 32.
     */
    public static void generatePAN(CloudCoin coin) {
        coin.pan = new String[Config.nodeCount];
        for (int i = 0; i < Config.nodeCount; i++) {
            SecureRandom random = new SecureRandom();
            byte[] cryptoRandomBuffer = random.generateSeed(16);

            UUID uuid = UUID.nameUUIDFromBytes(cryptoRandomBuffer);
            coin.pan[i] = uuid.toString().replace("-", "");
        }
    }

    /**
     * CloudCoin Constructor for importing a CloudCoin from a CSV file.
     *
     * @param csv      CSV file as a String.
     * @param folder   the folder containing the Stack file.
     * @param filename the absolute filepath of the Stack file.
     * @return a CloudCoin object.
     */
    public static CloudCoin cloudCoinFromCsv(String csv, String folder, String filename) {
        CloudCoin coin = new CloudCoin(folder, filename);

        try {
            String[] values = csv.split(",");

            coin.setSn(Integer.parseInt(values[0]));
            // values[1] is denomination.
            coin.setNn(Integer.parseInt(values[2]));
            ArrayList<String> ans = new ArrayList<>();
            for (int i = 0; i < Config.nodeCount; i++)
                ans.add(values[i + 3]);
            coin.setAn(ans);

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

        return coin;
    }

    /**
     * Converts a hexadecimal expiration date to String.
     *
     * @param edHex the hexadecimal expiration date.
     * @return the expiration date String.
     */
    public static String expirationDateHexToString(String edHex) {
        long monthsAfterZero = Long.valueOf(edHex, 16);
        LocalDate zeroDate = LocalDate.of(2016, 8, 13);
        LocalDate ed = zeroDate.plusMonths(monthsAfterZero);
        return ed.getMonthValue() + "-" + ed.getYear();
    }

    /**
     * Converts a hexadecimal pown value to String.
     *
     * @param hexString the hexadecimal pown String.
     * @return the pown String.
     */
    public static String pownHexToString(String hexString) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0, j = hexString.length(); i < j; i++) {
            if ('0' == hexString.charAt(i))
                stringBuilder.append('p');
            else if ('1' == hexString.charAt(i))
                stringBuilder.append('9');
            else if ('2' == hexString.charAt(i))
                stringBuilder.append('n');
            else if ('E' == hexString.charAt(i))
                stringBuilder.append('e');
            else if ('F' == hexString.charAt(i))
                stringBuilder.append('f');
        }

        return stringBuilder.toString();
    }
}
