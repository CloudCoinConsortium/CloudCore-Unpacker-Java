package com.cloudcoin.unpacker;

import com.cloudcoin.unpacker.core.Stack;
import com.cloudcoin.unpacker.util.CoinUtils;
import com.cloudcoin.unpacker.util.Utils;
import com.google.gson.JsonSyntaxException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
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


    /* Fields */

    public String rootFolder;
    public String importFolder;
    public String importedFolder;
    public String trashFolder;
    public String suspectFolder;

    /** A SimpleDateFormat for quickly fetching the current timestamp. A timestamp will be formatted as such:
     * <ul><li>-2000123011595999</li></ul>
     */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("-yyyyMMddHHmmssSSS");


    /* Constructors */

    /** Constructor for objects of class FileUtils */
    public FileUtils(String rootFolder, String importFolder, String importedFolder, String trashFolder, String suspectFolder) {
        this.rootFolder = rootFolder;
        this.importFolder = rootFolder + importFolder + File.separator;
        this.importedFolder = rootFolder + importedFolder + File.separator;
        this.trashFolder = rootFolder + trashFolder + File.separator;
        this.suspectFolder = rootFolder + suspectFolder + File.separator;

        createDirectories();
    }


    /* Methods */

    public boolean createDirectories() {
        try {
            Files.createDirectories(Paths.get(rootFolder));

            Files.createDirectories(Paths.get(importFolder));
            Files.createDirectories(Paths.get(importedFolder));
            Files.createDirectories(Paths.get(trashFolder));
            Files.createDirectories(Paths.get(suspectFolder));
        } catch (Exception e) {
            System.out.println("FS#CD: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Loads an array of CloudCoins from a Stack file.
     *
     * @param fullFilePath the absolute filepath of the Stack file.
     * @return ArrayList of CloudCoins.
     */
    public static ArrayList<CloudCoin> loadCloudCoinsFromStack(String fullFilePath) {
        try {
            String file = new String(Files.readAllBytes(Paths.get(fullFilePath)));
            Stack stack = Utils.createGson().fromJson(file, Stack.class);
            for (CloudCoin coin : stack.cc)
                coin.setFullFilePath(fullFilePath);
            return new ArrayList<>(Arrays.asList(stack.cc));
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
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
                System.out.println("A coin with that Serial Number already exists in the folder.");
                return false;
            }
            Path path = Paths.get(fullFileName);
            Files.createFile(path);
            Files.write(path, binary);
            return true;
        } catch (Exception e) {
            System.out.println("Error writing binary file: " + fullFileName);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Writes a CloudCoins a Stack file.
     *
     * @param coin     the ArrayList of CloudCoins.
     * @param filePath the absolute filepath of the CloudCoin file, without the extension.
     */
    public void writeCoinToIndividualStacks(CloudCoin coin, String filePath) {
        Stack stack = new Stack(coin);
        try {
            String target = ensureFilenameUnique(CoinUtils.generateFilename(coin), ".stack", filePath);
            Files.write(Paths.get(filePath + target), Utils.createGson().toJson(stack).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /** Moves a file from the Import folder to the Imported folder. */
    public void moveToImportedFolder(String fileName) {
        String source = importFolder + fileName;
        String name = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        String target = ensureFilenameUnique(name, extension, importedFolder);
        System.out.println("moving to " + target);
        new File(source).renameTo(new File(importedFolder + target));
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
        CoinUtils.generatePAN(cc);

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
            cc = parseJpeg(wholeString);
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
                cc.setNn(childJSONObject.getInt("nn"));
                cc.setSn(childJSONObject.getInt("sn"));
                JSONArray an = childJSONObject.getJSONArray("anBinary");
                cc.setAn(toStringArray(an));
                String ed = childJSONObject.getString("ed");
                JSONArray aoid = childJSONObject.getJSONArray("aoid");
                /*String[] strAoid = toStringArray(aoid);
                for (int j = 0; j < strAoid.length; j++) { //"fracked=ppppppppppppppppppppppppp"
                    if (strAoid[j].contains("=")) {//see if the string contains an equals sign
                        String[] keyvalue = strAoid[j].split("=");
                        cc.aoidOld.put(keyvalue[0], keyvalue[1]);//index 0 is the key index 1 is the value.
                    } else { //There is something there but not a key value pair. Treak it like a memo
                        cc.aoidOld.put("memo", strAoid[j]);
                    }//end if cointains an =
                }//end for each aoid*/
            }//end for each coin
        }//end if json
        cc.currentFilename = CoinUtils.getDenomination(cc) + ".CloudCoin." + cc.getNn() + "." + cc.getSn() + ".";

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
        ArrayList<String> ans = new ArrayList<>(25);
        for (int i = 0; i < 25; i++) {
            ans.add(i, wholeString.substring(startAn + (i * 32), endAn + (i * 32))); // System.out.println(i +": " +ans[i]);
            System.out.println("an: " + ans.get(i));
        }
        cc.setAn(ans);

        //cc.aoidOld = null;//wholeString.substring( 840, 895 );
        //cc.hp = 25;//Integer.parseInt(wholeString.substring( 896, 896 ), 16);
        cc.setEd(wholeString.substring(898, 902));
        cc.setNn(Integer.parseInt(wholeString.substring(902, 904), 16));
        cc.setSn(Integer.parseInt(wholeString.substring(904, 910), 16));

        CoinUtils.generatePAN(cc);
        return cc;
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
                jsonData += line + System.lineSeparator();
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
     * @param folderPath the folder to check for files.
     * @return String Array.
     */
    public static String[] selectFileNamesInFolder(String folderPath) {
        File folder = new File(folderPath);
        Collection<String> files = new ArrayList<>();
        if (folder.isDirectory()) {
            File[] filenames = folder.listFiles();

            if (null != filenames) {
                for (File file : filenames) {
                    if (file.isFile()) {
                        files.add(file.getName());
                    }
                }
            }
        }
        return files.toArray(new String[]{});
    }

    public static String ensureFilenameUnique(String filename, String extension, String folder) {
        if (!Files.exists(Paths.get(folder + filename + extension)))
            return filename + extension;

        filename = filename + '.';
        String newFilename;
        int loopCount = 0;
        do {
            newFilename = filename + Integer.toString(++loopCount);
        }
        while (Files.exists(Paths.get(folder + newFilename + extension)));
        return newFilename + extension;
    }

    /**
     * Converts a JSONArray to a String array
     *
     * @param jsonArray a JSONArray object
     * @return String[]
     */
    public static ArrayList<String> toStringArray(JSONArray jsonArray) {
        if (jsonArray == null)
            return null;

        ArrayList<String> arr = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < arr.size(); i++) {
            arr.set(i, jsonArray.optString(i));
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