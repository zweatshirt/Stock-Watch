package com.zach.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

/* StockListAdapter adapts view holders to recycler view */
public class StockListAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private final MainActivity main;
    private final List<Stock> stockList;

    public StockListAdapter(List<Stock> stockList, MainActivity main) {
        this.main = main;
        //this.stockList = new ArrayList<>(stocks.values());
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_entry, parent, false);

        itemView.setOnClickListener(main);
        itemView.setOnLongClickListener(main);

        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        holder.stockSymbol.setText(stock.getSymbol());

        if (checkNulls(stock)) {
            holder.stockName.setText(stock.getStockName());
//            String dblPriceStr = stock.getStockPrice();
            Double price = Double.parseDouble(stock.getStockPrice());
            DecimalFormat df = new DecimalFormat("##########.00");
            String priceStr = "$" + df.format(price);
            holder.stockPrice.setText(priceStr);

            Double percent = Double.parseDouble(stock.getStockPercent());
            percent = percent * 100.0;
            percent = Math.round(percent * 100.0) / 100.0;
            String s = stock.getStockChange() + " (" + percent + "%)";
            holder.stockPercent.setText(s);
            if (stock.getStockChange().contains("-")) {
                int red = Color.parseColor("#FF0000");
                holder.arrow.setImageResource(android.R.drawable.arrow_down_float);
                holder.arrow.setColorFilter(red);
                holder.stockSymbol.setTextColor(red);
                holder.stockName.setTextColor(red);
                holder.stockPercent.setTextColor(red);
                holder.stockPrice.setTextColor(red);
            }
            else {
                int green = Color.parseColor("#00FF00");
                holder.stockSymbol.setTextColor(green);
                holder.arrow.setColorFilter(green);
                holder.stockName.setTextColor(green);
                holder.stockPercent.setTextColor(green);
                holder.stockPrice.setTextColor(green);
            }

        } else {
            String emptyName = "No name loaded";
            holder.stockName.setText(emptyName);
            holder.stockPercent.setText("0%");
            holder.stockPrice.setText("$0");
        }
    }

    private boolean checkNulls(Stock stock) {
        if (stock.getStockPercent() == null && stock.getStockChange() == null && stock.getStockPrice() == null)
            return false;
        return !stock.getStockPrice().equals("null") && !stock.getStockChange().equals("null") && !stock.getStockPercent().equals("null");
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

}
