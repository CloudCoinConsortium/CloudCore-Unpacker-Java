package com.cloudcoin.bank.bank;


import org.json.JSONArray;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public  String receivedFolder;

    /**
     * Constructor for objects of class FileUtils
     */
    public FileUtils(String rootFolder, String importFolder, String importedFolder, String trashFolder, String receivedFolder)
    {
        // initialise instance variables
        this.rootFolder = rootFolder;
        this.importFolder = rootFolder + importFolder + File.separator;;
        this.importedFolder = rootFolder + importedFolder + File.separator;;
        this.trashFolder = rootFolder + trashFolder + File.separator;;
        this.receivedFolder = rootFolder + receivedFolder + File.separator;;
    }//End constructor

    public boolean writeBinaryToReceivedFolder(String fileName, byte[] binary){
        try {
            fileName = receivedFolder + fileName + ".coin";
            File file = new File(fileName);
            if (file.exists() && !file.isDirectory()) {
                System.out.println("A coin with that SN already exists in the folder.");
                return false;
            }
            Path path = Paths.get(fileName);
            Files.createFile(path);
            Files.write(path, binary);
            return true;
        } catch (Exception e) {
            System.out.println("Error writin binary file: " + fileName);
            e.printStackTrace();
            return false;
        }
    }

    public boolean writeStackToReceivedFolder(String fileName, String json){
        try{
            boolean goodSave = false;
             File file = new File( receivedFolder + fileName +".stack" );
             if(file.exists() && !file.isDirectory()) {
                //System.out.println("A coin with that SN already exists in the folder.");
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
        new File(source).renameTo(new File(target));
    }


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

    byte[] loadBinaryFromFile(String filepath) throws IOException {
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