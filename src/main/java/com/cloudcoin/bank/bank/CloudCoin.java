package com.cloudcoin.bank.bank;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


/**
 * Creats a CloudCoin
 * Represents one CloudCoin
 *
 * ImportStacks_CloudCoin currently has no differences from CloudCoin.
 *
 * @author Sean H. Worthington
 * @version 1/9/2016
 * @version 6/23/2018
 */
class CloudCoin
{
    // instance variables
    public int nn;//Network Numbers
    public int sn;//Serial Number
    public String[] ans = new String[25] ;//Authenticity Numbers
    public String[] pastStatus = new String[25] ;//fail, pass, error, unknown (could not connect to raida)
    public String ed; //Expiration Date expressed as a hex string like 97e2 Sep 2016
    public String aoid;
    public String fileName;
    public String json;
    public static final int YEARSTILEXPIRE = 2;
    public String extension; //

    public byte[] binary; // The CloudCoin binary file
    public byte coinMode; // 0 transfer mode, 1 internal use
    public byte[] coinName; // Name of coin in bytes
    public String coinNameText; // Name of coin in clear text
    public byte[] anTypes; // 4 hexadecimal numbers representing AN types: 0 (unknown) 1 (random number)
    // 2 (Hash of SN and pin/password) 3 (Hash of SN and email) 4 (Hash of SN and iris) 5 (Hash of SN and face)
    // 6 (Authority-assigned number) 7 (Hash of SN and fingerprint) 8 (Hash of SN and second user's pin)
    public byte[] pown; // Last Pown results: 0 (unknown) 1 (pass) 2 (no response) E (error) F (fail)
    public byte[] an; // Authenticity numbers

    /**
     * CloudCoin Constructor
     * This is used for importing new coins from the outside
     * @param nn Network Number
     * @param sn Serial Number
     * @param ans Authenticity Numbers
     * @param ed Expiration Date
     * @param aoid an array of strings like "memo=carpayment"
     */
    public CloudCoin( int nn, int sn, String[] ans )
    { // initialise instance variables
        this.nn = nn;
        this.sn = sn;
        this.ans = ans;
        this.ed = "";
        this.aoid = "";
        this.fileName = getDenomination() +".CloudCoin." + this.nn +"."+ this.sn + ".";
        for(int i = 0; i< 25; i++){
            pastStatus[i] = "u";//Set them all to undetected
        }//end for each pan
        this.json = setJSON();
    }

    /**
     * CloudCoin Constructor
     * This is used for importing new coins from the outside
     * @param nn Network Number
     * @param sn Serial Number
     * @param ans Authenticity Numbers
     * @param ed Expiration Date
     * @param aoid an array of strings like "memo=carpayment"
     */
    public CloudCoin( byte[] binary)
    { // initialise instance variables
        this.binary = binary;
        coinMode = binary[3];
        coinName = Arrays.copyOfRange(binary, 4, 12);
        coinNameText = new String(coinName, StandardCharsets.UTF_8);
        anTypes = Arrays.copyOfRange(binary, 13, 14);
        nn = binary[15];
        sn = (binary[18] & 0xff) | ((binary[17] & 0xff) << 8) | ((binary[16] & 0x0f) << 16);
        pown = Arrays.copyOfRange(binary, 19, 31);
        an = Arrays.copyOfRange(binary, 32, 431);
    }

     /**
     * Returns the denomination of the money based on the serial number
     * 
     * @param  sn Serial Numbers 
     * @return  1, 5, 25, 100, 250
     */
    
    public int getDenomination() 
    {
        int nom = 0;
        if(this.sn < 1 ){  nom = 0;}
        else if(this.sn < 2097153) {  nom = 1; } 
        else if (this.sn < 4194305) { nom = 5; } 
        else if (this.sn < 6291457) { nom = 25; } 
        else if (this.sn < 14680065) { nom = 100; } 
        else if (this.sn < 16777217) { nom = 250; } 
        else { nom = '0'; }
        return nom;
    }
    

    /**
     * Method setJSON creates JSON text version of the coin that can be written to file.
     * @return The return value is a String of JSON that can be written to hard drive. 
     */
    public String setJSON(){   
    
        json =  "\t\t{" + System.getProperty("line.separator") ;
        json += "\t\t\"nn\":\"1\"," + System.getProperty("line.separator");
        json +="\t\t\"sn\":\""+ sn + "\"," + System.getProperty("line.separator");
        json += "\t\t\"an\": [\"";
        for(int i = 0; i < 25; i++){
            json += ans[i];
            if( i == 4 || i == 9 || i == 14 || i == 19){
                json += "\"," + System.getProperty("line.separator") + "\t\t\t\"";
            }else if( i == 24){
                //json += "\""; last one do nothing
            }
            else
            {//end if is line break
                json += "\",\"";
            }//end else
        }//end for 25 ans
        json += "\"]," + System.getProperty("line.separator");//End of ans
        json += "\t\t\"ed\":\"9-2016\"," + System.getProperty("line.separator");
        json += "\t\t\"aoid\": [" + aoid + "]" + System.getProperty("line.separator");
        json += "\t\t}"+ System.getProperty("line.separator"); 
    
        //Allways change expiration date when saving (not a truley accurate but good enought )
        Date date= new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        year = year + YEARSTILEXPIRE;
        String expDate = month + "-" + year;
        json.replace("9-2016", expDate );
        return this.json;

    }//end get JSON

    /**
     * Method calcExpirationDate figures out when the coin will expire (after it has been authenticated)
     * This value is written to the coin's ed fields. 
     *
     */
    public void calcExpirationDate(){
        Date date= new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        year = year + YEARSTILEXPIRE;
        this.ed = month + "-" + year; 
    }//end calc exp date

}//End of class CloudCoin