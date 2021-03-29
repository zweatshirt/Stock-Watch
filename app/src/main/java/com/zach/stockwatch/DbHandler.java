package com.zach.stockwatch;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* SQLite Stonks Database */
public class DbHandler extends SQLiteOpenHelper {

    private static final String TAG = "DbHandler";
    private static final int DATABASE_VERSION = 7;
    private static final String DB_NAME = "StocksDB";
    // Table name
    private static final String TABLE_NAME = "Stock";

    // Columns
    private static final String SYMBOL = "Symbol";
    private static final String NAME = "Name";
    private static final String PRICE = "Price";
    private static final String PERCENT = "Percent";
    private static final String CHANGE = "Change";

    private final SQLiteDatabase db;
    private final MainActivity main;
    // Init table
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    SYMBOL + " TEXT not null unique," +
                    NAME + " TEXT," +
                    PRICE + " TEXT," +
                    PERCENT + " TEXT," +
                    CHANGE + " TEXT)";

    public DbHandler(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        main = (MainActivity) context; // might cause issue
        db = getWritableDatabase();

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_NAME);
        db.execSQL(SQL_CREATE_TABLE);
    }

    // load le stonks
    List<Stock> loadStocks() {
        List<Stock> stocks = new ArrayList<>();

        Cursor cursor = db.query(
                TABLE_NAME,
                new String[] {SYMBOL, NAME, PRICE, PERCENT, CHANGE},
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            cursor.moveToFirst();


            for (int i = 0; i < cursor.getCount(); i++) {
                String symbol = cursor.getString(0);
                String name = cursor.getString(1);
                String price = cursor.getString(2);
                String percent = cursor.getString(3);
                String change = cursor.getString(4);

                Stock stock = new Stock(symbol, name, price, percent, change);
                stocks.add(stock);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return stocks;
    }

    void addStock(Stock stock) {
        ContentValues vals = new ContentValues();
        vals.put(SYMBOL, stock.getSymbol());
        vals.put(NAME, stock.getStockName());
        vals.put(PRICE, stock.getStockPrice());
        vals.put(PERCENT, stock.getStockPercent());
        vals.put(CHANGE, stock.getStockChange());

        db.insert(TABLE_NAME, null, vals);
    }

    void updateStock(Stock stock) {
        ContentValues vals = new ContentValues();
        vals.put(SYMBOL, stock.getSymbol());
        vals.put(NAME, stock.getStockName());
        vals.put(PRICE, stock.getStockPrice());
        vals.put(PERCENT, stock.getStockPercent());
        vals.put(CHANGE, stock.getStockChange());

        db.update(TABLE_NAME, vals, SYMBOL + " = ?", new String[]{stock.getSymbol()});
    }

    void deleteStock(String symbol) {
        db.delete(TABLE_NAME, SYMBOL + " = ?", new String[]{symbol});
    }

    void findStock(HashMap<String, String> params) {

        StringBuilder details = new StringBuilder();
        for (String key : params.keySet()) {
            details.append(key)
                    .append(" = '")
                    .append(params.get(key))
                    .append("' AND ");
        }
        String clause = details.substring(0, details.lastIndexOf("AND"));

        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + clause, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                String symbol = cursor.getString(0);
                String name = cursor.getString(1);
                String price = cursor.getString(2);
                String percent = cursor.getString(3);
                String change = cursor.getString(4);

                Stock stock = new Stock(symbol, name, price, percent, change);
                main.showFindResults(stock);
            }
            else {
                main.showFindResults(null);
            }

            cursor.close();

        }
    }

    void shutDown() { db.close(); }

}
