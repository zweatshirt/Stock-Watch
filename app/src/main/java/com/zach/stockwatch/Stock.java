package com.zach.stockwatch;

import android.util.JsonWriter;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

/* Stock class
* Stock has:
* Symbol, name, price, percent, and change.
* Symbol and name should (ideally) never be null.
* Price, percent, and change can be null for the sake of using GetSymbols.
* Comparable interface never implemented, but should've been.
*/

public class Stock implements Serializable, Comparable<Stock> {
    private String symbol, stockName, stockChange;
    private String stockPrice, stockPercent;

    // constructor for updating user's displayed stocks, grabbing most recent stock info
    public Stock(String symbol, String stockName, String stockPrice, String stockPercent, String stockChange) {
        this.symbol = symbol;
        this.stockName = stockName;
        this.stockPrice = stockPrice;
        this.stockPercent = stockPercent;
        this.stockChange = stockChange;
    }

    /* constructor for stock symbol (abbreviation) and stock name,
    used for initial list that user can pick from
     */
    public Stock(String symbol, String name) {
        this(symbol, name, null, null, null);
    }

    public Stock (String symbol) {this(symbol, null, null, null, null); }

    public String getSymbol() {
        return symbol;
    }

    public String getStockName() {
        return stockName;
    }

    public String getStockPrice() {
        return stockPrice;
    }

    public String getStockPercent() {
        return stockPercent;
    }

    public String getStockChange() {
        return stockChange;
    }

    public void setStockChange(String stockChange) {
        this.stockChange = stockChange;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public void setStockPrice(String stockPrice) {
        this.stockPrice = stockPrice;
    }

    public void setStockPercent(String stockPercent) {
        this.stockPercent = stockPercent;
    }

    // toString designed specifically for JSONFile transfer
    // I was wondering if this is bad practice,
    // but it sure is convenient lol

    public String toString() {
        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setIndent(" ");
            jsonWriter.beginObject();
            jsonWriter.name("symbol").value(getSymbol());
            jsonWriter.name("name").value(getStockName());
            jsonWriter.name("percent").value(getStockPercent());
            jsonWriter.name("change").value(getStockChange());
            jsonWriter.endObject();
            jsonWriter.close();
            return stringWriter.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return ""; // I feel like there's a better way to do this
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stock)) return false;

        Stock stock = (Stock) o;
        return this.getSymbol() != null ? this.getSymbol().equals(stock.getSymbol()) : stock.getSymbol() == null;
    }

    @Override
    public int hashCode() {
        return this.getSymbol() != null ? this.getSymbol().hashCode() : 0;
    }

    @Override
    public int compareTo(Stock o) {
        return this.getSymbol().compareTo(o.getSymbol());
    }
}
