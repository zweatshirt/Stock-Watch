package com.zach.stockwatch;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/* View holder class for stock entries */
public class StockViewHolder extends RecyclerView.ViewHolder {
    TextView stockSymbol;
    TextView stockName;
    TextView stockPrice;
    TextView stockPercent;
    ImageView arrow;

    public StockViewHolder(@NonNull View itemView) {
        super(itemView);

        stockSymbol = itemView.findViewById(R.id.stock_abbreviation);
        stockName = itemView.findViewById(R.id.stock_name);
        stockPrice = itemView.findViewById(R.id.stock_price);
        stockPercent = itemView.findViewById(R.id.stock_percent);
        arrow = itemView.findViewById(R.id.arrow);
    }

}
