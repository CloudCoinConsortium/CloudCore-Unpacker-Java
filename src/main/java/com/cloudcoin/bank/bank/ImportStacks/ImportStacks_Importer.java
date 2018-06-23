package com.cloudcoin.bank.bank.ImportStacks;

import com.cloudcoin.bank.bank.CloudCoin;
import com.cloudcoin.bank.bank.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ImportStacks_Importer {

    public static boolean importStack(FileUtils fileUtils, String fileName) {
        String fileJson;
        try {
            fileJson = fileUtils.importJSON(fileName);
        } catch (IOException ex) {
            System.out.println("Error importing stack " + ex);
            return false;
        }
        JSONArray incomeJsonArray;
        try {
            JSONObject json = new JSONObject(fileJson);
            incomeJsonArray = json.getJSONArray("cloudcoin");
            CloudCoin tempCoin;
            for (int i = 0; i < incomeJsonArray.length(); i++) {
                JSONObject childJSONObject = incomeJsonArray.getJSONObject(i);
                int nn = childJSONObject.getInt("nn");
                int sn = childJSONObject.getInt("sn");
                JSONArray an = childJSONObject.getJSONArray("an");
                String[] ans = FileUtils.toStringArray(an);
                String ed = childJSONObject.getString("ed");

                tempCoin = new CloudCoin(nn, sn, ans, ed, null, "suspect");
                fileUtils.writeTo(fileUtils.suspectFolder, tempCoin);
                fileUtils.moveToImportedFolder(fileName);
            }
        } catch (JSONException ex) {
            System.out.println("Stack File " + fileName + " Corrupt. See CloudCoin file api and edit your stack file: " + ex);
            return false;
        }
        return true;
    }
}
