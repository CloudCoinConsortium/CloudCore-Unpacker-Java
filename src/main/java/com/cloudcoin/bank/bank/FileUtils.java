package com.cloudcoin.bank.bank;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Help to read, write and change files.
 *
 * ImportStacks_FileUtils has the following differences from FileUtils:
 * Uses ImportStacks_CloudCoin instead of CloudCoin.
 * Uses the following methods from FileUtils:
 * - ImportStacks_CloudCoinFromFile()
 * - deleteCoin()
 * - importJSON()
 * - loadJSON();
 * - moveToImportedFolder()
 * - moveToTrashFolder()
 * - parseJpeg()
 * - selectFileNamesInFolders()
 * - setJSON()
 * - toHexidecimal
 * - toStringArray()
 * - writeTo()
 * - writeToSuspectFolder()
 *
 * @author Sean H. Worthington
 * @version 1/17/2017
 */
class FileUtils
{
    // instance variables
    public  String rootFolder;
    public  String importFolder;
    public  String importedFolder;
    public  String trashFolder;
    public  String suspectFolder;

    SimpleDateFormat dateFormat = new SimpleDateFormat("-yyyyMMddHHmmssSSS");

    /**
     * Constructor for objects of class FileUtils
     */
    public FileUtils(String rootFolder, String importFolder, String importedFolder, String trashFolder, String suspectFolder)
    {
        // initialise instance variables
        this.rootFolder = rootFolder;
        this.importFolder = rootFolder + importFolder + File.separator;;
        this.importedFolder = rootFolder + importedFolder + File.separator;;
        this.trashFolder = rootFolder + trashFolder + File.separator;;
        this.suspectFolder = rootFolder + suspectFolder + File.separator;;
    }//End constructor

    public boolean writeBinaryToReceivedFolder(String fileName, byte[] binary){
        String fullFileName = suspectFolder + fileName + ".coin";

        try {
            File file = new File(fullFileName);
            if (file.exists() && !file.isDirectory()) {
                System.out.println("A coin with that SN already exists in the folder.");
                return false;
            }
            Path path = Paths.get(fullFileName);
            Files.createFile(path);
            Files.write(path, binary);
            return true;
        } catch (Exception e) {
            System.out.println("Error writin binary file: " + fullFileName);
            e.printStackTrace();
            return false;
        }
    }

    public boolean writeTo( String folder, CloudCoin cc){
        boolean goodSave = false;
        String json = setJSON( cc );
        File f = new File( folder + cc.fileName +".stack" );
        if(f.exists() && !f.isDirectory()) {
            System.out.println("A jpg/jpeg with that SN already exists in the folder.");
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

    public boolean writeStackToReceivedFolder(String fileName, String json){
        try{
            boolean goodSave = false;
             File file = new File( suspectFolder + fileName +".stack" );
             if(file.exists() && !file.isDirectory()) {
                 System.out.println("A stack with that SN already exists in the folder.");
               return goodSave;
            }
            FileOutputStream is = new FileOutputStream(file);
                OutputStreamWriter osw = new OutputStreamWriter(is);
                Writer w = new BufferedWriter(osw);
                w.write(json);
                w.close();
            return true;
        }catch(IOException e){
         return false;

        }
    }//end write to received folder

      public void moveToImportedFolder(String fileName){
        String source = importFolder + fileName;
        String target = importedFolder + fileName;
        new File(source).renameTo(new File(target));
    }


    public void moveToTrashFolder(String fileName){
        String source = importFolder + fileName;
        String target = trashFolder + fileName;

        System.out.println("Moving " + source + " to " + target);
        boolean result = new File(source).renameTo(new File(target));
        if (!result) {
            moveToTrashFolderFailsafe(fileName);
        }
    }

    private void moveToTrashFolderFailsafe(String fileName){
        String fileNameHalf = fileName.substring(0, fileName.lastIndexOf('.'));
        String fileNameExtension = fileName.substring(fileName.lastIndexOf('.'));

        String newFileName = fileNameHalf + dateFormat.format(new Date()) + fileNameExtension;

        String source = importFolder + fileName;
        String target = trashFolder + newFileName;

        System.out.println("Moving " + source + " to " + target);
        boolean result = new File(source).renameTo(new File(target));
        if (!result) {
            moveToTrashFolderFailsafe(fileName);
        }
    }



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
            //this.newCoins = new ImportStacks_CloudCoin[incomeJsonArray.length()];
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

    private CloudCoin parseJpeg(String wholeString){
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

    String loadJSON( String jsonfile) {
        String jsonData = "";
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader( importFolder + jsonfile ));
            while ((line = br.readLine()) != null) {
                jsonData += line + "\n";
            }
        } catch (IOException e) {
            System.out.println("Failed to open " + jsonfile);
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
    }//end json test

    byte[] loadBinaryFromFile(String filepath) throws IOException, InterruptedException {
        // Ensure that the file exists.
        File file = new File(importFolder + filepath);
        if (!file.exists()) {
            wait(100);
            if (!file.exists())
                return new byte[0];
        }

        // Create a lock to ensure that no other program has access to the file.
        FileChannel lockFile = new RandomAccessFile(file, "rw").getChannel();
        lockFile.lock();
        lockFile.close();

        Path path = Paths.get(importFolder + filepath);
        return Files.readAllBytes(path);
    }


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
     * Method toHexadecimal
     *
     * @param digest An array of bytes that will change into a string of hex characters
     * @return A string version of the bytes in hex form.
     */
    public static String toHexadecimal(byte[] digest){
        String hash = "";
        for(byte aux : digest) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) hash += "0";
            hash += Integer.toHexString(b);
        }
        return hash;
    }
}