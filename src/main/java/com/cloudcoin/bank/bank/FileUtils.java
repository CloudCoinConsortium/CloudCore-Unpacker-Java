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
 * FileUtils contains methods for simplifying common File operations.
 *
 * @author Ben Ward
 * @version 7/5/2018
 */
class FileUtils {

    // instance variables

    public String rootFolder;
    public String importFolder;
    public String importedFolder;
    public String trashFolder;
    public String suspectFolder;

    /** A SimpleDateFormat for quickly fetching the current timestamp. A timestamp will be formatted as such:
     * <ul>
     * <li>-2000123011595999</li>
     * </ul> */
    SimpleDateFormat dateFormat = new SimpleDateFormat("-yyyyMMddHHmmssSSS");

    /** Constructor for objects of class FileUtils */
    public FileUtils(String rootFolder, String importFolder, String importedFolder, String trashFolder, String suspectFolder) {
        this.rootFolder = rootFolder;
        this.importFolder = rootFolder + importFolder + File.separator;
        this.importedFolder = rootFolder + importedFolder + File.separator;
        this.trashFolder = rootFolder + trashFolder + File.separator;
        this.suspectFolder = rootFolder + suspectFolder + File.separator;
    }

    /**
     * Writes a binary-encoded CloudCoin to a new file.
     *
     * @return {@code true} if a new file is created and written to; {@code false} otherwise
     */
    public boolean writeBinaryToReceivedFolder(String fileName, byte[] binary) {
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

    /**
     * Writes a CloudCoin object to a new file.
     *
     * @return {@code true} if a new file is created and written to; {@code false} otherwise
     */
    public boolean writeTo(String folder, CloudCoin cc) {
        boolean goodSave = false;
        String json = setJSON(cc);
        File f = new File(folder + cc.fileName + ".stack");
        if (f.exists() && !f.isDirectory()) {
            System.out.println("A jpg/jpeg with that SN already exists in the folder.");
            return goodSave;
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(folder + cc.fileName + ".stack"));
            // System.out.println("\nSaving Coin file to Bank/" + this.fileName + extension );
            String wholeJson = "{" + System.getProperty("line.separator");
            wholeJson += "\t\"cloudcoin\": [" + System.getProperty("line.separator");
            wholeJson += json;
            wholeJson += "\t] " + System.getProperty("line.separator");
            wholeJson += "}";
            writer.write(wholeJson);
            goodSave = true;
        } catch (IOException e) {
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
            }
        }
        return goodSave;
    }

    /**
     * Writes a json-encoded CloudCoin to a new file.
     *
     * @return {@code true} if a new file is created and written to; {@code false} otherwise
     */
    public boolean writeStackToReceivedFolder(String fileName, String json) {
        try {
            boolean goodSave = false;
            File file = new File(suspectFolder + fileName + ".stack");
            if (file.exists() && !file.isDirectory()) {
                System.out.println("A stack with that SN already exists in the folder.");
                return goodSave;
            }
            FileOutputStream is = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            w.write(json);
            w.close();
            return true;
        } catch (IOException e) {
            return false;

        }
    }

    /** Moves a file from the Import folder to the Imported folder. */
    public void moveToImportedFolder(String fileName) {
        String source = importFolder + fileName;
        String target = importedFolder + fileName;
        new File(source).renameTo(new File(target));
    }

    /** Moves a file from the Import folder to the Trash folder. If the operation fails, attempt to rename the file. */
    public void moveToTrashFolder(String fileName) {
        String source = importFolder + fileName;
        String target = trashFolder + fileName;

        System.out.println("Moving " + source + " to " + target);
        boolean result = new File(source).renameTo(new File(target));
        if (!result) {
            moveToTrashFolderFailsafe(fileName);
        }
    }

    /**
     * Renames and moves a file from the Import folder to the Trash folder. This method is used when the filename is
     * already in use. The filename will be appended with the current time specified by {@link FileUtils#dateFormat}.
     */
    private void moveToTrashFolderFailsafe(String fileName) {
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

    /**
     * Attempts to parse a CloudCoin from a jpg/jpeg file.
     *
     * @param loadFilePath the filepath pointing to a CloudCoin file
     * @return {@link CloudCoin}
     * @throws FileNotFoundException if the file is not found
     * @throws IOException if there was an error reading the file
     */
    public CloudCoin cloudCoinFromFile(String loadFilePath) throws FileNotFoundException, IOException {
        String extension = "";
        CloudCoin cc = new CloudCoin();
        //put some default values
        for (int i = 0; i < 25; i++) {
            cc.pans[i] = cc.generatePan();
            cc.pastStatus[i] = "undetected";
        }//end for each pan

        /*SEE IF FILE IS JPEG OR JSON*/
        int indx = loadFilePath.lastIndexOf('.');
        if (indx > 0) {
            extension = loadFilePath.substring(indx + 1);
        }
        //System.out.println("Loading file: " + loadFilePath);
        if (extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("jpg")) {//JPEG
            FileInputStream fis;
            byte[] jpegHeader = new byte[455];
            String wholeString = "";
            // try {
            fis = new FileInputStream(loadFilePath);
            fis.read(jpegHeader);// read bytes to the buffer
            wholeString = toHexadecimal(jpegHeader);// System.out.println(wholeString);
            fis.close();
            parseJpeg(wholeString);
            //} catch (FileNotFoundException e) { // TODO Auto-generated catch block
            //  e.printStackTrace();
            //} catch (IOException e) { // TODO Auto-generated catch block
            //  e.printStackTrace();
            //}
        } else {//json image
            String incomeJson = "";
            //try{
            incomeJson = loadJSON(loadFilePath);
            //}catch( IOException ex ){
            //   System.out.println( "Error loading file path " + ex );

            //}
            JSONArray incomeJsonArray;
            //try{
            JSONObject o = new JSONObject(incomeJson);
            incomeJsonArray = o.getJSONArray("cloudcoin");
            //this.newCoins = new ImportStacks_CloudCoin[incomeJsonArray.length()];
            for (int i = 0; i < incomeJsonArray.length(); i++) {  // **line 2**
                JSONObject childJSONObject = incomeJsonArray.getJSONObject(i);
                cc.nn = childJSONObject.getInt("nn");
                cc.sn = childJSONObject.getInt("sn");
                JSONArray an = childJSONObject.getJSONArray("an");
                cc.ans = toStringArray(an);
                String ed = childJSONObject.getString("ed");
                JSONArray aoid = childJSONObject.getJSONArray("aoid");
                String[] strAoid = toStringArray(aoid);
                for (int j = 0; j < strAoid.length; j++) { //"fracked=ppppppppppppppppppppppppp"
                    if (strAoid[j].contains("=")) {//see if the string contains an equals sign
                        String[] keyvalue = strAoid[j].split("=");
                        cc.aoid.put(keyvalue[0], keyvalue[1]);//index 0 is the key index 1 is the value.
                    } else { //There is something there but not a key value pair. Treak it like a memo
                        cc.aoid.put("memo", strAoid[j]);
                    }//end if cointains an =
                }//end for each aoid
            }//end for each coin
        }//end if json
        cc.fileName = cc.getDenomination() + ".CloudCoin." + cc.nn + "." + cc.sn + ".";
        cc.json = "";
        cc.jpeg = null;

        return cc;
    }

    /**
     * Attempt to parse a CloudCoin from a jpg/jpeg header.
     *
     * @param wholeString the string representation of the jpg/jpeg header
     * @return {@link CloudCoin}
     */
    private CloudCoin parseJpeg(String wholeString) {
        CloudCoin cc = new CloudCoin();
        int startAn = 40;
        int endAn = 72;
        for (int i = 0; i < 25; i++) {
            cc.ans[i] = wholeString.substring(startAn + (i * 32), endAn + (i * 32)); // System.out.println(i +": " +ans[i]);
        }

        cc.aoid = null;//wholeString.substring( 840, 895 );
        cc.hp = 25;//Integer.parseInt(wholeString.substring( 896, 896 ), 16);
        cc.ed = wholeString.substring(898, 902);
        cc.nn = Integer.parseInt(wholeString.substring(902, 904), 16);
        cc.sn = Integer.parseInt(wholeString.substring(904, 910), 16);

        for (int i = 0; i < 25; i++) {
            cc.pans[i] = cc.generatePan();
            cc.pastStatus[i] = "undetected";
        }
        return cc;
    }

    /**
     * Converts a CloudCoin object to a JSON-encoded String. This is used when reading CloudCoin from a jpg/jpeg file.
     *
     * @return String
     */
    public String setJSON(CloudCoin cc) {
        String json = "\t\t{" + System.getProperty("line.separator");
        json += "\t\t\"nn\":\"1\"," + System.getProperty("line.separator");
        json += "\t\t\"sn\":\"" + cc.sn + "\"," + System.getProperty("line.separator");
        json += "\t\t\"an\": [\"";
        for (int i = 0; i < 25; i++) {
            json += cc.ans[i];
            if (i == 4 || i == 9 || i == 14 || i == 19) {
                json += "\"," + System.getProperty("line.separator") + "\t\t\t\"";
            } else if (i == 24) {
                //json += "\""; last one do nothing
            } else {//end if is line break
                json += "\",\"";
            }//end else
        }//end for 25 ans
        json += "\"]," + System.getProperty("line.separator");//End of ans
        json += "\t\t\"ed\":\"9-2016\"," + System.getProperty("line.separator");
        String aoids = "";
        if (cc.aoid == null) {
            aoids = "";
        } else {
            Enumeration<String> e = cc.aoid.keys();
            int count = 0;
            while (e.hasMoreElements()) {
                if (count != 0) {
                    aoids += ",";
                }
                String k = e.nextElement();
                System.out.println("\"" + k + "=" + cc.aoid.get(k) + "\"");
                count++;
            }
        }

        String strAoid = "\"" + cc.aoid + "\"";//add quotation marks to the string for jason
        if (cc.aoid == null) {
            strAoid = "";//aoid is mull so don't need any quot marks.
        }
        //strAoids will have {} brackeds added for some reason. Strip them.
        strAoid = strAoid.replace("{", "");
        strAoid = strAoid.replace("}", "");
        json += "\t\t\"aoid\": [" + strAoid + "]" + System.getProperty("line.separator");
        json += "\t\t}" + System.getProperty("line.separator");


        //Allways change expiration date when saving (not a truley accurate but good enought )
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        year = year + cc.YEARSTILEXPIRE;
        String expDate = month + "-" + year;
        json.replace("9-2016", expDate);
        return json;

    }


    /**
     * Attempts to read a JSON object from a file.
     *
     * @param jsonFilePath the filepath pointing to the JSON file
     * @return String
     */
    String loadJSON(String jsonFilePath) {
        String jsonData = "";
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader(importFolder + jsonFilePath));
            while ((line = br.readLine()) != null) {
                jsonData += line + "\n";
            }
        } catch (IOException e) {
            System.out.println("Failed to open " + jsonFilePath);
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
    }

    /**
     * Attempts to read a binary array from a file.
     *
     * @param filepath the filepath pointing to the binary file
     * @return String
     */
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

    /**
     * Returns an array containing all filenames in a directory.
     *
     * @param directoryPath the directory to check for files
     * @return String[]
     */
    public String[] selectFileNamesInFolder(String directoryPath) {
        File dir = new File(directoryPath);
        String candidateFileExt = "";
        Collection<String> files = new ArrayList<String>();
        if (dir.isDirectory()) {
            File[] listFiles = dir.listFiles();

            for (File file : listFiles) {
                if (file.isFile()) {//Only add files with the matching file extension
                    files.add(file.getName());
                }
            }
        }
        return files.toArray(new String[]{});
    }

    /**
     * Converts a JSONArray to a String array
     *
     * @param jsonArray a JSONArray object
     * @return String[]
     */
    public static String[] toStringArray(JSONArray jsonArray) {
        if (jsonArray == null)
            return null;

        String[] arr = new String[jsonArray.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = jsonArray.optString(i);
        }
        return arr;
    }

    /**
     * Converts a byte array to a Hexadecimal String.
     *
     * @param digest the byte array to convert to a Hexadecimal String
     * @return String
     */
    public static String toHexadecimal(byte[] digest) {
        String hash = "";
        for (byte aux : digest) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) hash += "0";
            hash += Integer.toHexString(b);
        }
        return hash;
    }
}