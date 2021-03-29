package com.zach.stockwatch;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/* GetSymbols class gets all stock symbols (and names) by running a new thread. */
public class GetSymbols implements Runnable {

    private final MainActivity main;
    private static final String TAG = "GetInfoRunnable";
    private final String STOCK_SYM_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    private final DbHandler db;
    private final boolean debug = false;

    public GetSymbols(MainActivity main) {
        this.main = main;
        db = new DbHandler(main);
    }

    @Override
    public void run() {
        Uri uri;
        String urlString;
        URL url;
        HttpsURLConnection connection;
        BufferedReader reader;
        InputStream connectionInputStream;
        StringBuilder builder = new StringBuilder();
        try {
            // STOCKS_URL directs to JSON array with stock symbols and names to be extracted
            uri = Uri.parse(STOCK_SYM_URL);
            urlString = uri.toString();
            url = new URL(urlString);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                Log.d(TAG, "GetSymbols run: HTTP ResponseCode NOT OK: " + connection.getResponseCode());
                finalResults(null);
                return;
            }

            connectionInputStream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(connectionInputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
            finalResults(null);
        }

        finalResults(builder.toString());
    }

    private List<Stock> parseJSONForSym(String result) {
        List<Stock> stockList = new ArrayList<>();

        try {
            JSONArray stockInfoArr = new JSONArray(result);

            for (int i = 0; i < stockInfoArr.length(); i++) {
                JSONObject stockObj = (JSONObject) stockInfoArr.get(i);
                String symbol = stockObj.getString("symbol");
                String name = stockObj.getString("name");
                stockList.add(new Stock(symbol, name));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stockList;
    }

    public void finalResults(String result) {
        // if connection failed or other exception
        if (result == null) {
            Log.d(TAG, "finalResults: FAILURE to download stock information.");
            main.runOnUiThread(main::failedSymDownload);
        }
        else {
            // if connection OK, populate stocksList
            final List<Stock> stockList = parseJSONForSym(result);
            main.runOnUiThread(() -> {
                Log.d(TAG, "Loaded " + stockList.size() + " stocks.");
                if (debug) {
                    Toast.makeText(main, "Loaded " + stockList.size() + " stocks.", Toast.LENGTH_LONG).show();
                }
                main.updateSymbolList(stockList);

            });
        }
    }


}




