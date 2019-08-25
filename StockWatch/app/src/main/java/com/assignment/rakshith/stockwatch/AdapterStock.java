package com.assignment.rakshith.stockwatch;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class AdapterStock extends RecyclerView.Adapter<AdapterStock.ViewHolder>{
    private List<Stock>  stockList;
    private MainActivity mainActivity;
    private DecimalFormat df = new DecimalFormat("##.##");

    public AdapterStock(List<Stock> stockList, MainActivity mainActivity) {
        this.stockList = stockList;
        this.mainActivity = mainActivity;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout ConstraintLayout;
        public TextView SymbolView;
        public TextView CompView;
        public TextView PriceView;
        public TextView NewPriceView;
        public ImageView ImageView;

        public ViewHolder(ConstraintLayout itemView) {
            super(itemView);
            ConstraintLayout = itemView;
            SymbolView = itemView.findViewById(R.id.textViewSymbol);
            CompView = itemView.findViewById(R.id.textViewCompanyName);
            PriceView = itemView.findViewById(R.id.textViewPrice);
            NewPriceView= itemView.findViewById(R.id.textViewPriceChange);
            ImageView= (ImageView) itemView.getViewById(R.id.changeImage);

        }
    }


    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        ConstraintLayout v = (ConstraintLayout)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_row_layout, parent, false);
        v.setOnClickListener(mainActivity);
        v.setOnLongClickListener(mainActivity);
        ViewHolder viewhold = new ViewHolder(v);
        return viewhold;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final Stock stock = stockList.get(i);

        if(Math.abs(stock.getPriceChange())==stock.getPriceChange()){
            viewHolder.SymbolView.setTextColor(mainActivity.getResources().getColor(R.color.colorGreenDark));
            viewHolder.CompView.setTextColor(mainActivity.getResources().getColor(R.color.colorGreenDark));
            viewHolder.PriceView.setTextColor(mainActivity.getResources().getColor(R.color.colorGreenDark));
            viewHolder.NewPriceView.setTextColor(mainActivity.getResources().getColor(R.color.colorGreenDark));
            viewHolder.ImageView.setBackgroundResource(R.drawable.icon_up);

        }else{
            viewHolder.SymbolView.setTextColor(mainActivity.getResources().getColor(R.color.colorRedDark));
            viewHolder.CompView.setTextColor(mainActivity.getResources().getColor(R.color.colorRedDark));
            viewHolder.PriceView.setTextColor(mainActivity.getResources().getColor(R.color.colorRedDark));
            viewHolder.NewPriceView.setTextColor(mainActivity.getResources().getColor(R.color.colorRedDark));
            viewHolder.ImageView.setBackgroundResource(R.drawable.icon_down);
        }

        viewHolder.SymbolView.setText(stock.getStockSymbol());
        viewHolder.CompView.setText(stock.getCompanyName());
        viewHolder.PriceView.setText(""+stock.getPrice());
        viewHolder.NewPriceView.setText(df.format(stock.getPriceChange()) + "(" + df.format(stock.getPercentChange()) + "%)");
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }




}
