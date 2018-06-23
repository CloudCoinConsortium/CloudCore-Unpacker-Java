package com.cloudcoin.bank.bank;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Help to read, write and change files
 * 
 * @author Sean H. Worthington
 * @version 1/17/2017
 */
public class FileUtils
{
    // instance variables
    public  String rootFolder; 
    public  String importFolder;
    public  String importedFolder; 
    public  String trashFolder;
    public  String suspectFolder; 
    public  String frackedFolder; 
    public  String bankFolder; 
    public  String templateFolder; 
    public  String counterfeitFolder; 
    public  String directoryFolder; 
    public  String exportFolder; 

    /**
     * Constructor for objects of class FileUtils
     */
    public FileUtils(String rootFolder, String importFolder, String importedFolder, String trashFolder, String suspectFolder, String frackedFolder, String bankFolder, String templateFolder, String counterfeitFolder, String directoryFolder, String exportFolder)
    {
        // initialise instance variables
        this.rootFolder = rootFolder ;
        this.importFolder = importFolder;
        this.importedFolder = importedFolder;
        this.trashFolder = trashFolder;
        this.suspectFolder = suspectFolder;
        this.frackedFolder = frackedFolder;
        this.bankFolder = bankFolder;
        this.templateFolder = templateFolder;
        this.counterfeitFolder = counterfeitFolder;
        this.directoryFolder = directoryFolder;
        this.exportFolder = exportFolder;
    }//End constructor

    public CloudCoin cloudCoinFromFile( String loadFilePath ) throws FileNotFoundException, IOException {
        String extension ="";
        CloudCoin cc = new CloudCoin();
        //put some default values
        for(int i = 0; i< 25; i++){
            cc.pans[i] = cc.generatePan();
            cc.pastStatus[i] = "undetected";
        }//end for each pan
        
        /*SEE IF FILE IS JPEG OR JSON*/
        int indx = loadFilePath.lastIndexOf('.');
        if (indx > 0) {
            extension = loadFilePath.substring(indx+1);
        }
        //System.out.println("Loading file: " + loadFilePath);
        if( extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("jpg")){//JPEG
            FileInputStream fis;
            byte[] jpegHeader = new byte[455];
            String wholeString ="";
           // try {
                fis = new FileInputStream( loadFilePath );
                fis.read(jpegHeader);// read bytes to the buffer
                wholeString = toHexadecimal( jpegHeader );// System.out.println(wholeString);
                fis.close(); 
                parseJpeg( wholeString );
            //} catch (FileNotFoundException e) { // TODO Auto-generated catch block
              //  e.printStackTrace();
            //} catch (IOException e) { // TODO Auto-generated catch block
              //  e.printStackTrace();
            //}
        }else{//json image
            String incomeJson = ""; 
            //try{ 
                incomeJson = loadJSON( loadFilePath );
            //}catch( IOException ex ){  
             //   System.out.println( "Error loading file path " + ex );
                
            //}
            JSONArray incomeJsonArray;
            //try{  
                JSONObject o = new JSONObject( incomeJson );
                incomeJsonArray = o.getJSONArray("cloudcoin");
                //this.newCoins = new CloudCoin[incomeJsonArray.length()];
                for (int i = 0; i < incomeJsonArray.length(); i++) {  // **line 2**
                    JSONObject childJSONObject = incomeJsonArray.getJSONObject(i);
                    cc.nn     = childJSONObject.getInt("nn");
                    cc.sn     = childJSONObject.getInt("sn");
                    JSONArray an = childJSONObject.getJSONArray("an");
                    cc.ans = toStringArray(an);
                    String ed     = childJSONObject.getString("ed");
                    JSONArray aoid     = childJSONObject.getJSONArray("aoid");
                    String[] strAoid = toStringArray(aoid);
                    for(int j =0; j< strAoid.length; j++  ){ //"fracked=ppppppppppppppppppppppppp"
                        if( strAoid[j].contains("=") ){//see if the string contains an equals sign
                        String[] keyvalue = strAoid[j].split("=");
                         cc.aoid.put(keyvalue[0], keyvalue[1]);//index 0 is the key index 1 is the value.
                        }else{ //There is something there but not a key value pair. Treak it like a memo
                          cc.aoid.put("memo",strAoid[j] );
                        }//end if cointains an = 
                    }//end for each aoid
                }//end for each coin
        }//end if json
        cc.fileName = cc.getDenomination() +".CloudCoin." + cc.nn +"."+ cc.sn + ".";
        cc.json = "";
        cc.jpeg = null;
        
        return cc;
    }//end loadCloudCoin
    
    
    public String importJSON( String jsonfile) throws FileNotFoundException {
        String jsonData = "";
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader( importFolder + jsonfile ));
            while ((line = br.readLine()) != null) {
                jsonData += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                return "";
            }
        }
        return jsonData;
    }//en d json test

    
    /**
     * Method setJSON creates JSON text version of the coin that can be written to file.
     *
     * @return The return value is a String of JSON that can be written to hard drive. 
     */
    public String setJSON( CloudCoin cc){   
        
        String json =  "\t\t{" + System.getProperty("line.separator") ;
        json += "\t\t\"nn\":\"1\"," + System.getProperty("line.separator");
        json +="\t\t\"sn\":\""+ cc.sn + "\"," + System.getProperty("line.separator");
        json += "\t\t\"an\": [\"";
        for(int i = 0; i < 25; i++){
            json += cc.ans[i];
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
        String aoids = "";
        if(  cc.aoid == null) {
               aoids = "";
         } else {
          Enumeration<String> e = cc.aoid.keys();
            int count =0;
          while(e.hasMoreElements()) {
            if( count != 0){ aoids += ",";}  
            String k = e.nextElement();
            System.out.println("\"" + k + "=" + cc.aoid.get(k) + "\"");
            count++;
          }
        }
        
        String strAoid =  "\"" + cc.aoid + "\"";//add quotation marks to the string for jason
        if ( cc.aoid == null){
            strAoid = "";//aoid is mull so don't need any quot marks. 
        }
        //strAoids will have {} brackeds added for some reason. Strip them. 
        strAoid =  strAoid.replace("{","");
        strAoid =  strAoid.replace("}","");
        json += "\t\t\"aoid\": [" + strAoid + "]" + System.getProperty("line.separator");
        json += "\t\t}"+ System.getProperty("line.separator"); 
        

        //Allways change expiration date when saving (not a truley accurate but good enought )
        Date date= new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        year = year + cc.YEARSTILEXPIRE;
        String expDate = month + "-" + year;
        json.replace("9-2016", expDate );
        return json;

    }//end get JSON

    /**
     * Method setJpeg creates an array of bytes that can be written to file to make a jpg image based on the CloudCoin
     *
     * @ param rootFolder This points to the template that will be used to make the jpg image. Templates can be customized.
     */
    public byte[] makeJpeg( CloudCoin cc){
        byte[] returnBytes =  null;
        //Make byte array from CloudCoin
        String cloudCoinStr ="";
        for( int i = 0; i < 25; i++  ){
            cloudCoinStr += cc.ans[i];
        }//end for each an
        // cloudCoinStr +="Defeat tyrants and obey God0"; //27 AOID and comments
        cloudCoinStr +="204f42455920474f4420262044454645415420545952414e54532000"; //Hex for " OBEY GOD & DEFEAT TYRANTS "
        cloudCoinStr +="00";//LHC = 100%
        cloudCoinStr +="97E2";//0x97E2;//Expiration date Sep. 2018
        cloudCoinStr += "01";// cc.nn;//network number
        String hexSN = Integer.toHexString(cc.sn);  
        String fullHexSN ="";
        switch (hexSN.length())//Add leading zeros to the hex number
        {
            case 1: fullHexSN = "00000" +hexSN; break;
            case 2:fullHexSN = "0000" +hexSN; break;
            case 3:fullHexSN = "000" +hexSN; break;
            case 4:fullHexSN = "00" +hexSN; break;
            case 5:fullHexSN = "0" +hexSN; break;
            case 6:fullHexSN = hexSN; break;
        }
        cloudCoinStr += fullHexSN;
        String Path = "";
        switch( cc.getDenomination() ){
            case   1:  Path jpeg1 = Paths.get( templateFolder +"jpeg1.jpg");//This is the location of the template for 1s
            try{ returnBytes = Files.readAllBytes(jpeg1); }catch(IOException e){
                System.out.println("General I/O exception: " + e.getMessage()); e.printStackTrace();
            }//end catch
            break;
            case   5: 
            Path jpeg5 = Paths.get(templateFolder +"jpeg5.jpg");//This is the location of the template for 5s
            try{ returnBytes = Files.readAllBytes(jpeg5); }catch(IOException e){
                System.out.println("General I/O exception: " + e.getMessage());
                e.printStackTrace(); }//end catch
            break;
            case  25: 
            Path jpeg25 = Paths.get(templateFolder +"jpeg25.jpg");
            try{ returnBytes = Files.readAllBytes(jpeg25); }catch(IOException e){
                System.out.println("General I/O exception: " + e.getMessage());
                e.printStackTrace();}//end catch
            break;
            case 100:
            Path jpeg100 = Paths.get(templateFolder +"jpeg100.jpg");
            try{ returnBytes = Files.readAllBytes(jpeg100); }catch(IOException e){
                System.out.println("General I/O exception: " + e.getMessage()); e.printStackTrace();
            }//end catch
            break;
            case 250: 
            Path jpeg250 = Paths.get(templateFolder +"jpeg250.jpg");
            try{ returnBytes = Files.readAllBytes(jpeg250); }catch(IOException e){ 
                System.out.println("General I/O exception: " + e.getMessage()); e.printStackTrace();
            }//end catch
            break;
        }//end switch
        /*OVERWRITE */
        byte[] ccArray = hexStringToByteArray( cloudCoinStr );
        int offset = 20;  // System.out.println("ccArray length " + ccArray.length);
        for( int j =0; j < ccArray.length; j++  ){
            returnBytes[offset + j ] = ccArray[j];
        }//end for each byte in the ccArray
        return returnBytes;
    }//end get jpeg
    
    
    
    public void moveToTrashFolder(String fileName){
        String source = importFolder + fileName;
        String target = trashFolder + fileName;
        new File(source).renameTo(new File(target));
    }

    public void moveToImportedFolder( String fileName ){
        String source = importFolder + fileName;
        String target = importedFolder + fileName;
        new File(source).renameTo(new File(target));
    }

    public void writeToSuspectFolder( String fileName ){
        String source = importFolder + fileName;
        String target = suspectFolder + fileName;
        new File(source).renameTo(new File(target));
    }

    /***
     * Given directory path return an array of strings of all the files in the directory.
     * @parameter directoryPath The location of the directory to be scanned
     * @return filenames The names of all the files in the directory
     */
    public String[] selectFileNamesInFolder(String directoryPath) {
        File dir = new File(directoryPath);
        String candidateFileExt = "";
        Collection<String> files  =new ArrayList<String>();
        if(dir.isDirectory()){
            File[] listFiles = dir.listFiles();

            for(File file : listFiles){
                if(file.isFile()) {//Only add files with the matching file extension
                    files.add(file.getName());
                }
            }
        }
        return files.toArray(new String[]{});
    }//End select all file names in a folder

    public static String[] toStringArray(JSONArray array) {
        if(array==null)
            return null;

        String[] arr=new String[array.length()];
        for(int i=0; i<arr.length; i++) {
            arr[i]=array.optString(i);
        }
        return arr;
    }//end toStringArray

    /**
     * Method deleteCoin
     *
     * @param path The folder that the coin is located in
     * @return true if the coin is deleted and false if it does not get deleted. 
     */
    public boolean deleteCoin( String path ){
        boolean deleted = false;
        File f  = new File( path ); //System.out.println("Deleteing Coin: "+path + this.fileName + extension);
        try {
            deleted = f.delete(); if(deleted){ }else{
                // System.out.println("Delete operation is failed.");
            }//end else
        }catch(Exception e){
            e.printStackTrace();
        }
        return deleted;
    }//end delete file

    /**
     * Method concatArrays Takes two arrays and adds them together
     *
     * @param a The first array
     * @param b The second array
     * @return The arrays together
     */
    public String[] concatArrays(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c= new String[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    /**
     * Method ifFileExists Checks to see if a file exists
     *
     * @param filePathString A parameter
     * @return True if the file exists, false if the file does not exist.
     */
    public boolean ifFileExists( String filePathString ){
        File f = new File(filePathString);
        if(f.exists() && !f.isDirectory()) { 
            return true;
        }
        return false;
    }//end if file Exists

    /**
     * Method loadFileToString
     *
     * @param jsonfile The path and anme of a file that contains CloudCoins in the form of JSON.
     * @return The return value
     */
    public String loadFileToString( String jsonfile) throws FileNotFoundException {
        String jsonData = "";
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader( jsonfile ));
            while ((line = br.readLine()) != null) {
                jsonData += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return jsonData;
    }//end load file to string

    public int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1)
            pos = str.indexOf(substr, pos + 1);
        return pos;
    }

    /***
     * Given a string and a file name, write the string to the harddrive.
     * @parameter text The string to go into the file
     * @paramerter filename The name to be given to the file.
     */
    public boolean stringToFile( String text, String filename) {
        boolean writeGood =  false;
        try(  PrintWriter out = new PrintWriter( filename )  ){
            out.println( text );
            writeGood = true;
        }catch( FileNotFoundException ex){
            System.out.println(ex);
        }
        return writeGood;
    }//end string to file 
    
       /**
     * Method toHexadecimal
     *
     * @param digest An array of bytes that will change into a string of hex characters
     * @return A string version of the bytes in hex form. 
     */
    private String toHexadecimal(byte[] digest){
        String hash = "";
        for(byte aux : digest) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) hash += "0";
            hash += Integer.toHexString(b);
        }
        return hash;
    }

    
    public boolean writeTo( String folder, CloudCoin cc){
        boolean goodSave = false;
        String json = setJSON( cc );
        File f = new File( folder + cc.fileName +".stack" );
        if(f.exists() && !f.isDirectory()) { 
            System.out.println("A coin with that SN already exists in the folder.");
            return goodSave;
        }

        BufferedWriter writer = null;
        try{
            writer = new BufferedWriter( new FileWriter( folder + cc.fileName +".stack"  ));
            // System.out.println("\nSaving Coin file to Bank/" + this.fileName + extension );
            String wholeJson =   "{" + System.getProperty("line.separator") ;
            wholeJson +=   "\t\"cloudcoin\": [" + System.getProperty("line.separator") ;
            wholeJson += json;
            wholeJson += "\t] "+ System.getProperty("line.separator"); 
            wholeJson += "}";  
            writer.write( wholeJson );
            goodSave = true;
        }catch ( IOException e){ } finally{    try{
                if ( writer != null)
                    writer.close( );
            }catch ( IOException e){}
        }
        return goodSave;
    } 
    
    private CloudCoin parseJpeg( String wholeString){
        CloudCoin cc = new CloudCoin();
        int startAn = 40;
        int endAn = 72;
        for(int i = 0; i< 25; i++){
            cc.ans[i] = wholeString.substring( startAn +(i*32), endAn +(i*32) ); // System.out.println(i +": " +ans[i]);
        }//end for

        cc.aoid = null;//wholeString.substring( 840, 895 );
        cc.hp = 25;//Integer.parseInt(wholeString.substring( 896, 896 ), 16);
        cc.ed = wholeString.substring( 898, 902   );
        cc.nn = Integer.parseInt(wholeString.substring( 902, 904 ), 16);
        cc.sn = Integer.parseInt(wholeString.substring( 904, 910 ), 16);

        for(int i = 0; i< 25; i++){
            cc.pans[i] = cc.generatePan();
            cc.pastStatus[i] = "undetected";
        }//end for each pan
        return cc;
    }//end parse Jpeg
    
    
    
    private String loadJSON( String jsonfile) throws FileNotFoundException {
        String jsonData = "";
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader( jsonfile ));
            while ((line = br.readLine()) != null) {
                jsonData += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return jsonData;
    }//en d json test
  
    

    /**
     * Method hexStringToByteArray turns a String of hex characters into bytes
     *
     * @param s A String of hex characters
     * @return A byte array that represents the string of hex characters
     */
    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }//End of hexString to byte array

       /**
     * Method writeJpeg writes a jpg image to the hard drive. 
     *
     * @param path The full path to the file except the file name.
     * @param tag A parameter that adds a tag to the filename
     * @return true if file is saved, false if file is not saved
     */
    public boolean writeJpeg( String path, String tag, byte[] jpeg, String fileName  ) throws IOException, FileNotFoundException{  
        boolean writeGood = true;
        String file = path + File.separator  + fileName + tag +".jpg";
        //System.out.println("Saving jpg: " + file);
        try{
            Files.write(Paths.get( file ), jpeg );
        }catch( IOException ex ){
            //  System.out.println( "Error Saving Jpeg: " + ex );
            writeGood = false;
        }//end try ioexception
        return writeGood;
    }//end jpeg to file

}
