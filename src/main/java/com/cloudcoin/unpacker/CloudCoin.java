package com.cloudcoin.unpacker;

import com.cloudcoin.unpacker.core.Config;
import com.cloudcoin.unpacker.util.CoinUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
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
    private int nn;
    @Expose
    @SerializedName("sn")
    private int sn;
    @Expose
    @SerializedName("an")
    private ArrayList<String> an = new ArrayList<>(Config.nodeCount);
    @Expose
    @SerializedName("ed")
    private String ed;
    @Expose
    @SerializedName("pown")
    private String pown = "uuuuuuuuuuuuuuuuuuuuuuuuu";
    @Expose
    @SerializedName("aoid")
    private ArrayList<String> aoid = new ArrayList<>();


    /* Fields */

    public transient String[] pan = new String[Config.nodeCount];


    // Binary variables

    public transient byte[] pownBinary; // Last Pown results: 0 (unknown) 1 (pass) 2 (no response) E (error) F (fail)
    public transient byte[] anBinary; // Authenticity numbers


    // JPEG variables

    public transient String folder;

    public transient String currentFilename;


    /* Constructors */


    /**
     * Simple CloudCoin constructor for setting the filepath of the coin. This is used when deleting or renaming a file.
     *
     * @param folder   the folder containing the Stack file.
     * @param filename the absolute filepath of the Stack file.
     */
    public CloudCoin(String folder, String filename) {
        this.currentFilename = filename;
    }

    /** CloudCoin Constructor for importing jpg/jpeg files. */
    public CloudCoin() {

    }

    /**
     * CloudCoin Constructor for importing new coins from a binary file.
     *
     * @param binary the binary array for a CloudCoin.
     */
    public CloudCoin(byte[] binary) {
        //this.binary = binary;
        //coinMode = binary[3];
        //coinName = Arrays.copyOfRange(binary, 4, 12);
        //coinNameText = new String(coinName, StandardCharsets.UTF_8);
        //anTypes = Arrays.copyOfRange(binary, 13, 14);
        setNn(binary[15]);
        setSn((binary[18] & 0xff) | ((binary[17] & 0xff) << 8) | ((binary[16] & 0x0f) << 16));
        //pownBinary = Arrays.copyOfRange(binary, 19, 31); // may be removed in the future.
        anBinary = Arrays.copyOfRange(binary, 32, 431);

        currentFilename = CoinUtils.getDenomination(this) + ".CloudCoin." + getNn() + "." + getSn() + ".";
    }


    /* Methods */

    /**
     * Returns a human readable String describing the contents of the CloudCoin.
     *
     * @return a String describing the CloudCoin.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("cloudcoin: (nn:").append(getNn()).append(", sn:").append(getSn());
        if (null != getEd()) builder.append(", ed:").append(getEd());
        if (null != pown) builder.append(", pown:").append(pown);
        if (null != aoid) builder.append(", aoid:").append(aoid.toString());
        if (null != anBinary) builder.append(", anBinary:").append(anBinary.toString());

        return builder.toString();
    }


    /* Getters and Setters */

    public int getNn() { return nn; }
    public int getSn() { return sn; }
    public ArrayList<String> getAn() { return an; }
    public String getEd() { return ed; }

    public void setNn(int nn) { this.nn = nn; }
    public void setSn(int sn) { this.sn = sn; }
    public void setAn(ArrayList<String> an) { this.an = an; }
    public void setEd(String ed) { this.ed = ed; }
    public void setPown(String pown) { this.pown = pown; }

    public void setFullFilePath(String fullFilePath) {
        this.folder = fullFilePath.substring(0, 1 + fullFilePath.lastIndexOf(File.separatorChar));
        this.currentFilename = fullFilePath.substring(1 + fullFilePath.lastIndexOf(File.separatorChar, fullFilePath.length()));
    }
}