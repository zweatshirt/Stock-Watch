package com.zach.stockwatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/* Zachery Linscott | CS 442 Project 3
* 'Stock Watch' Application
*
* Note to self:
* This app is mainly comprised of two lists, one that contains a list of stock symbols
* for the user to choose from, named 'stockSymbols,' populated with the GetSymbols runnable,
* that reads from an online JSON file.
* Upon selection, the 'stocks' list is populated with the most recent
* information about the user's saved stocks, by creating a new thread
* with runnable GetInfo. GetInfo sends queries to grab the most recently updated stock info.
* SwipeOnRefresh listener updates user stocks with the most recent information.
* User stock info is saved upon app exit, and loaded back in upon start up.
* For this part, the UpdateInfo thread is also needed.
*
* User search displays all stocks that contain the user's search string.
* onClick sends user to stock on website.
* onLongClick brings up dialog to delete stock.
*
* Green and up arrow: positive change
* Red and down arrow: negative change
 */

// TODO: Test when no network connection.
//  Markets are closed so I could never check update on refresh

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, View.OnLongClickListener {
    //private final Map<String, Stock> stocks = Collections.synchronizedMap(new HashMap<>());
    private List<Stock> stocks = new ArrayList<>();
    private List<Stock> stockSymbols = new ArrayList<>();
    private RecyclerView sView;
    private StockListAdapter adapter;
    private SwipeRefreshLayout swipe;
    private final String TAG = "MainActivity";

    private DbHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sView = findViewById(R.id.stock_rview);
        swipe = findViewById(R.id.swipe);

        // refactor, just too tired currently
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkConnected()) {
                    if (stockSymbols.isEmpty()) {
                        new Thread(new GetSymbols(MainActivity.this)).start();
                    }
                }
                if (!stocks.isEmpty()) {
                    doRefresh(true);
                    if (isNetworkConnected())
                        new Thread(new UpdateInfo(MainActivity.this, stocks)).start();
                }
                doRefresh(false);
            }
        });

        adapter = new StockListAdapter(stocks, this);
        sView.setAdapter(adapter);
        sView.setLayoutManager(new LinearLayoutManager(this));
        dbHandler = new DbHandler(this);

        stocks.clear();
        List<Stock> temp = new ArrayList<>(dbHandler.loadStocks());
        // load stock symbols data for search and select
        if (isNetworkConnected()) {
            new Thread(new GetSymbols(this)).start();
            if (!temp.isEmpty())
                new Thread(new UpdateInfo(this, temp)).start();
        }
        else {
            for (Stock tempStock : temp) {
                tempStock.setStockPercent("0");
                tempStock.setStockChange("0.00");
                tempStock.setStockPrice("0.00");
            }
            stocks.addAll(temp);
            adapter.notifyDataSetChanged();
        }
        // else showNoConnectionDialog();
    }

    public void doRefresh(boolean yes) {
        swipe.setRefreshing(yes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opt_menu, menu);
        return true;
    }

    // if I ever have the chance: REFACTOR. This is straight up ugly.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // menu options selection
        if (item.getItemId() == R.id.add_stock) {
            if(isNetworkConnected()) {
                LayoutInflater inflater = LayoutInflater.from(this);
                @SuppressLint("InflateParams")
                final View view = inflater.inflate(R.layout.dialog, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(view);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText et = (EditText) view.findViewById(R.id.user_query);
                        String userQuery = et.getText().toString();
                        if (isUpperCase(userQuery)) {
                            List<String> stocksToDisplay = new ArrayList<>();
                            for (int i = 0; i < stockSymbols.size(); i++) {
                                if (stockSymbols.get(i).getSymbol().contains(userQuery)
                                        || stockSymbols.get(i).getStockName().contains(userQuery))
                                {
                                    stocksToDisplay.add(stockSymbols.get(i).getSymbol());
                                }
                            }
                            if (isNetworkConnected()) {
                                if (stocksToDisplay.size() == 1)  {
                                    String stockToAdd = stocksToDisplay.get(0);
                                    doRefresh(true);
                                    addStockToList(stockToAdd);
                                }
                                else if (stocksToDisplay.size() > 1) {
                                    makeNewListDialog(stocksToDisplay);
                                }
                                else symbolNotFoundDialog(userQuery);
                            }
                            else showNoConnectionDialog();
                        }
                        else Toast.makeText(MainActivity.this,
                            "Stock symbols must be fully capitalized", Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.show();
            }
            else {
                showNoConnectionDialog();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // I had to create custom dialogs every time bc .setTitle() and .setMessage()
    // decided to stop working for ALL of my dialogs on this project.
    public void showNoConnectionDialog() {
        doRefresh(false);
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.no_connection_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        TextView title = (TextView) view.findViewById(R.id.noConnTitle);
        TextView desc = (TextView) view.findViewById(R.id.noConnMsg);
        String titleStr = "No Network Connection";
        String descStr = "Stocks Cannot Be Added Without A Network Connection";
        title.setText(titleStr);
        desc.setText(descStr);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void symbolNotFoundDialog(String userQuery) {
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.not_found_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        TextView title = (TextView) view.findViewById(R.id.notFoundTitle);
        String titleStr = "Symbol Not Found: " + userQuery;
        title.setText(titleStr);

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showDuplicateStockDialog(String stockSym) {
        doRefresh(false);
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.duplicate_stock_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        TextView title = (TextView) view.findViewById(R.id.noConnTitle);
        TextView desc = (TextView) view.findViewById(R.id.noConnMsg);
        String titleStr = "Duplicate Stock";
        String descStr = "Stock Symbol " + stockSym + " is already displayed";
        title.setText(titleStr);
        desc.setText(descStr);

        AlertDialog alert = builder.create();
        alert.show();
    }

    // NOTE: .setTitle() does not work on my end.
    // I did not want to create an entire custom list dialog.
    public void makeNewListDialog(List<String> stocksToDisplay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // will not work:
        builder.setTitle("Make a selection");

        CharSequence[] userOptions =
                stocksToDisplay.toArray(new CharSequence[stocksToDisplay.size()]);
        builder.setItems(userOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String stockToAdd = userOptions[which].toString();
                doRefresh(true);
                addStockToList(stockToAdd);
            }
        });
        builder.setPositiveButton("Nevermind", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // write user stock data to stocks.json when onDestroy and onStop occur
    @Override
    protected void onDestroy() {
        dbHandler.shutDown();
        super.onDestroy();
    }

//    @Override
//    protected void onStop() {
//        dbHandler.shutDown();
//        super.onStop();
//    }

    // stock symbols failed to download
    public void failedSymDownload() {
        stockSymbols.clear();
        Toast.makeText(MainActivity.this,
                "Failure to download symbols", Toast.LENGTH_LONG).show();
    }

    // stock info on update thread failed to download
    public void failedInfoDownload(){
        Toast.makeText(MainActivity.this,
                "Failure to download info", Toast.LENGTH_LONG).show();
        stocks.clear();
        adapter.notifyDataSetChanged();
    }

    // method called on end of GetSymbols thread run
    public void updateSymbolList(List<Stock> stockList) {
        stockSymbols.clear();
        stockSymbols.addAll(stockList);
    }

    // method called on end of UpdateInfo thread run
    public void updateUserStocks(List<Stock> stockListForInfo) {
        // if this doesn't work, set references instead
        stocks.clear();
        stocks.addAll(stockListForInfo);
        adapter.notifyDataSetChanged();
        doRefresh(false);
    }

    // check connectivity of user
    private boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    // method for onClick to go to stock website
    public void goToStockUrl(String stockSymbol) {
        String url = "https://www.marketwatch.com/investing/stock/" + stockSymbol;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    // user search input needs to meet upper case condition
    private boolean isUpperCase(String toTest) {
        return toTest.equals(toTest.toUpperCase());
    }

    // sort the stonks
    private void sortStonks() {
        stocks.sort(new Comparator<Stock>() {
            @Override
            public int compare(Stock o1, Stock o2) {
                return o1.getSymbol().compareToIgnoreCase(o2.getSymbol());
            }
        });
    }

    // check if stock already exists in stock list
    private boolean isDuplicate(Stock stock) {
//        return stocks.contains(stock);
        for (Stock s : stocks) {
            if (s.getSymbol().equals(stock.getSymbol()))
                return true;
        }
        return false;
    }

    // add stock to list and update DB
    private void addStockToList(String stockSym) {
        Stock stock = new Stock(stockSym);
        if (!isDuplicate(stock)) {
            stocks.add(0, stock);
            sortStonks();
            dbHandler.addStock(stock);
            new Thread(new UpdateInfo(MainActivity.this, stocks)).start();
        }
        else showDuplicateStockDialog(stockSym);
    }

    // go to stock on website onClick
    @Override
    public void onClick(View v) {
        int pos = sView.getChildLayoutPosition(v);
        if (stocks != null) {
            if (isNetworkConnected()) {
                String stockSym = stocks.get(pos).getSymbol();
                goToStockUrl(stockSym);}
            else showNoConnectionDialog();
        }
    }

    // delete selected stock on user long click
    @Override
    public boolean onLongClick(View v) {  // long click listener, delete if long click
        int pos = sView.getChildAdapterPosition(v);

        // all because builder.setTitle and builder.setMessage wouldn't work..
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.delete_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        TextView title = (TextView) view.findViewById(R.id.noConnTitle);
        TextView desc = (TextView) view.findViewById(R.id.noConnMsg);
        String titleStr = "Delete Stock";
        String descStr = "Delete Stock Symbol " + stocks.get(pos).getSymbol() + "?";
        title.setText(titleStr);
        desc.setText(descStr);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteStock(pos);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }

    // delete stock from database and stocks array list
    public void deleteStock(int position) {
        dbHandler.deleteStock(stocks.get(position).getSymbol());
        stocks.remove(position);
        adapter.notifyDataSetChanged();
    }

    // debug method
    public void showFindResults(Stock stock) {
        Log.d(TAG, "" + stock);
    }

    // refactor back into options menu code
    // collect stocks that match user query
//    private List<String> symbolDisplayList(String userQuery) {
//        List<String> stocksToDisplay = new ArrayList<>();
//        for (int i = 0; i < stockSymbols.size(); i++) {
//            if (stockSymbols.get(i).getSymbol().contains(userQuery)) {
//                stocksToDisplay.add(stockSymbols.get(i).getSymbol());
//                Log.d(TAG, "" + stocksToDisplay.toString());
//            }
//        }
//        return stocksToDisplay;
//    }

    // Initially didn't realize we had to implement the SQLite DB
    // leaving this here because I'd honestly prefer using JSON if I decide to extend this app
//    private List<Stock> loadFile() {
//        String fileName = getString(R.string.file_name);
//        List<Stock> fileStocks = new ArrayList<>();
//        try {
//
//            InputStream iStream = getApplication().getApplicationContext().openFileInput(fileName);
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(iStream, StandardCharsets.UTF_8));
//
//            StringBuilder builder = new StringBuilder();
//            String line;
//
//            while ((line = reader.readLine()) != null)
//                builder.append(line);
//
//            JSONArray jsonArray = new JSONArray(builder.toString());
//
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject jsonObj = jsonArray.getJSONObject(i);
//                String stockName = jsonObj.getString("name");
//                String stockSymbol = jsonObj.getString("symbol");
//                String stockPrice = jsonObj.getString("price");
//                String stockChange = jsonObj.getString("change");
//                String percentage = jsonObj.getString("percentage");
//                Stock stock = new Stock(stockSymbol, stockName, stockPrice, percentage, stockChange);
//                fileStocks.add(stock);
//            }
//
//        }
//        catch (FileNotFoundException e) {
//            Toast.makeText(this, "JSON File " + fileName + " not present.", Toast.LENGTH_SHORT).show();
//        }
//        catch (IOException | JSONException e) {
//            e.printStackTrace();
//        }
//
//        return fileStocks;
//    }
    // save data when onStop and onDestroy occurs
//    private void saveData() {
//        try {
//            FileOutputStream outStream = getApplicationContext()
//                    .openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);
//
//            PrintWriter printWriter = new PrintWriter(outStream);
//            printWriter.print(stocks);
//            printWriter.close();
//            outStream.close();
//        }
//        catch(IOException e) {
//            e.printStackTrace();
//        }
//    }





}