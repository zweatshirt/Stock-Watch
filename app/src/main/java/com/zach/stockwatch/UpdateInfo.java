package com.zach.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/* UpdateInfo creates new thread to update all stocks
* TODO: implement connection pooling
*/
public class UpdateInfo implements Runnable {
    private final String TAG = "UpdateInfo";
    private final String API_KEY = "pk_c2d89c0c18ce49759167f0ba4be986db";
    private final String STOCK_INFO_URL = "https://cloud.iexapis.com/stable/stock/";
    private final List<Stock> stockListForInfo;
    private final MainActivity main;
    private DbHandler db;

    public UpdateInfo(MainActivity main, List<Stock> stockListForInfo) {
        this.main = main;
        //this.stockListForInfo = new ArrayList<>(stocks.values());

        // want to update db by handler as stocks are updated
        // passing the main context seems iffy here
        db = new DbHandler(main);
        // wanted new object instead of working with the same ref
        this.stockListForInfo = new ArrayList<>(stockListForInfo);
    }

    @Override
    public void run() {
        Uri uri;
        String urlString;
        URL url;
        HttpsURLConnection connection;

        BufferedReader reader;
        InputStream connectionInputStream;
        StringBuilder builder;
        StringBuilder queryString;
        boolean hasFailed = false;

        try {
            if (stockListForInfo != null && !stockListForInfo.isEmpty()) { // overkill
                for (int i = 0; i < stockListForInfo.size(); i++) {

                    queryString = new StringBuilder();
                    queryString.append(STOCK_INFO_URL);
                    queryString.append(stockListForInfo.get(i).getSymbol());
                    queryString.append("/quote?token=" + API_KEY);

                    uri = Uri.parse(queryString.toString());
                    urlString = uri.toString();
                    url = new URL(urlString);
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                        Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + connection.getResponseCode());
                        finalResults(hasFailed = true);
                        return;
                    }

                    connectionInputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(connectionInputStream));

                    builder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    parseJSONForInfo(builder.toString(), i);
                }
            } else hasFailed = true;
        } catch(Exception e) {
            e.printStackTrace();
            hasFailed = true;
        }
        finally { finalResults(hasFailed); }
    }

    private void parseJSONForInfo(String result, int index) {
        try {
            JSONObject stockInfoObj = new JSONObject(result);
            // wondering if there's any nasty side effects from this, like formatting differences
            String stockName = stockInfoObj.getString("companyName");
            String stockPrice = stockInfoObj.getString("latestPrice");
            String stockChange = stockInfoObj.getString("change");
            String stockPercentage = stockInfoObj.getString("changePercent");
            stockListForInfo.get(index).setStockName(stockName);
            stockListForInfo.get(index).setStockPrice(stockPrice);
            stockListForInfo.get(index).setStockChange(stockChange);
            stockListForInfo.get(index).setStockPercent(stockPercentage);

            // update db stock
            db.updateStock(stockListForInfo.get(index));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void finalResults(boolean hasFailed) {
        if (hasFailed) {
            Log.d(TAG, "Failure in updating stocks");
            main.runOnUiThread(main::failedInfoDownload);
        }
        else {
            Log.d(TAG, "" + stockListForInfo.get(0).getSymbol() + stockListForInfo.get(0).getStockName());
            main.runOnUiThread(() -> main.updateUserStocks(stockListForInfo));
        }
    }

}
