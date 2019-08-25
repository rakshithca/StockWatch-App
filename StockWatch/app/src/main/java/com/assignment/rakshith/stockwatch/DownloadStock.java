package com.assignment.rakshith.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadStock extends AsyncTask<String, Void, String> {

    private static final String TAG = "DownloadStock";
    private MainActivity mainActivity;
    private final String Stock_Download_Link = "https://api.iextrading.com/1.0/stock/";
    private final String Stock_Download_Trail_Link = "/quote?displayPercent=true";
    Stock stock = new Stock();

    public DownloadStock(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mainActivity.updateStockList(stock);
    }

    @Override
    protected String doInBackground(String... strings) {

        Uri.Builder buildURL = Uri.parse(Stock_Download_Link + strings[0] + Stock_Download_Trail_Link).buildUpon();
        String urlToUse = buildURL.build().toString();
        Log.d(TAG, "doInBackground: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {

            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "doInBackground: Response Code: " + conn.getResponseCode() + ", " + conn.getResponseMessage());

            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "doInBackground: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }

        parseJSONData(sb.toString());
        return null;
    }


    void parseJSONData(String s) {
        try {
            JSONObject obj = new JSONObject(s);
            stock.setStockSymbol(obj.getString("symbol"));
            stock.setCompanyName(obj.getString("companyName"));
            stock.setPrice(obj.getDouble("latestPrice"));
            stock.setPriceChange(obj.getDouble("change"));
            stock.setPercentageChange(obj.getDouble("changePercent"));

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
