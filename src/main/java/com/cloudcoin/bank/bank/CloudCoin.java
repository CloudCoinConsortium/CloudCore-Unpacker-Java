package com.cloudcoin.bank.bank;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/**
 * A CloudCoin object represents a single monetary denomination. A {@code CloudCoin} can be identified with its Serial
 * Number, and validated with its Authenticity Numbers.
 *
 * @author Ben Ward
 * @version 7/5/2018
 */
public class CloudCoin {


    /* JSON Fields */

    @Expose
    @SerializedName("nn")
    public int nn;
    @Expose
    @SerializedName("sn")
    public int sn;
    @Expose
    @SerializedName("an")
    public ArrayList<String> an = new ArrayList<>(25);
    @Expose
    @SerializedName("ed")
    public String ed;
    @Expose
    @SerializedName("pown")
    public String pown = "uuuuuuuuuuuuuuuuuuuuuuuuu";
    @Expose
    @SerializedName("aoid")
    public ArrayList<String> aoid = new ArrayList<>();

    // instance variables
    public String[] pastStatus = new String[25];//fail, pass, error, unknown (could not connect to raida)
    public String aoidText;
    public String fileName;
    public String json;
    public static final int YEARSTILEXPIRE = 2;
    public String extension; //


    // Binary variables

    public byte[] binary; // The CloudCoin binary file
    public byte coinMode; // 0 transfer mode, 1 internal use
    public byte[] coinName; // Name of coin in bytes
    public String coinNameText; // Name of coin in clear text
    public byte[] anTypes; // 4 hexadecimal numbers representing AN types: 0 (unknown) 1 (random number)
    // 2 (Hash of SN and pin/password) 3 (Hash of SN and email) 4 (Hash of SN and iris) 5 (Hash of SN and face)
    // 6 (Authority-assigned number) 7 (Hash of SN and fingerprint) 8 (Hash of SN and second user's pin)
    public byte[] pownBinary; // Last Pown results: 0 (unknown) 1 (pass) 2 (no response) E (error) F (fail)
    public byte[] anBinary; // Authenticity numbers


    // JPEG variables

    /** The coin represented as a Byte array that can be written to a .jpg file. */
    public byte[] jpeg;

    /** Proposed Authenticity Numbers. 25 GUIDs without hyphens that will replace the ANs. */
    public String[] pans = new String[25];

    /**
     * Added Owner Indexed Data: The owner of the coin can use this space to put an array of their own data. This is an array of strings.
     * Each string is in the form of a key value pair seperated by an equals sign like "fracked=pppppppppppppppppppppppp".
     */
    public Dictionary aoidOld = new Hashtable();

    public transient String folder;

    public transient String currentFilename;


    /** CloudCoin Constructor for importing jpg/jpeg files. */
    public CloudCoin() {

    }

    /**
     * CloudCoin Constructor for importing new coins from a binary file.
     *
     * @param binary the binary array for a CloudCoin.
     */
    public CloudCoin(byte[] binary) {
        this.binary = binary;
        coinMode = binary[3];
        coinName = Arrays.copyOfRange(binary, 4, 12);
        coinNameText = new String(coinName, StandardCharsets.UTF_8);
        anTypes = Arrays.copyOfRange(binary, 13, 14);
        nn = binary[15];
        sn = (binary[18] & 0xff) | ((binary[17] & 0xff) << 8) | ((binary[16] & 0x0f) << 16);
        pownBinary = Arrays.copyOfRange(binary, 19, 31);
        anBinary = Arrays.copyOfRange(binary, 32, 431);

        fileName = getDenomination() + ".CloudCoin." + nn + "." + sn + ".";
    }

    /**
     * Returns a human readable String describing the contents of the CloudCoin.
     *
     * @return a String describing the CloudCoin.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("cloudcoin: (nn:").append(nn).append(", sn:").append(sn);
        if (null != ed) builder.append(", ed:").append(ed);
        if (null != pownBinary) builder.append(", pown:").append(pownBinary);
        if (null != aoidOld) builder.append(", aoid:").append(aoidOld.toString());
        if (null != anBinary) builder.append(", anBinary:").append(anBinary.toString());

        return builder.toString();
    }


    public String FileName() {
        return this.getDenomination() + ".CloudCoin." + nn + "." + sn + ".";
    }

    /**
     * Returns the denomination of the money based on the serial number
     *
     * @return {@code int}: 1, 5, 25, 100, or 250
     */
    public int getDenomination() {
        int nom;
        if (this.sn < 1) {
            nom = 0;
        } else if (this.sn < 2097153) {
            nom = 1;
        } else if (this.sn < 4194305) {
            nom = 5;
        } else if (this.sn < 6291457) {
            nom = 25;
        } else if (this.sn < 14680065) {
            nom = 100;
        } else if (this.sn < 16777217) {
            nom = 250;
        } else {
            nom = 0;
        }
        return nom;
    }

    /** Calculates the expiration date, after it has been authenticated. */
    public void calcExpirationDate() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        year = year + YEARSTILEXPIRE;
        this.ed = month + "-" + year;
    }

    /**
     * Generates secure random GUIDs for pans. Primarily used for the {@link CloudCoin#pans} variable. An example:
     * <ul>
     * <li>8d3eb063937164c789474f2a82c146d3</li>
     * </ul>
     * These Strings are hexadecimal and have a length of 32.
     *
     * @return String
     */
    public String generatePan() {
        String AB = "0123456789ABCDEF";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(25);
        for (int i = 0; i < 32; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public void setFullFilePath(String fullFilePath) {
        this.folder = fullFilePath.substring(0, 1 + fullFilePath.lastIndexOf(File.separatorChar));
        this.currentFilename = fullFilePath.substring(1 + fullFilePath.lastIndexOf(File.separatorChar, fullFilePath.length()));
    }
}