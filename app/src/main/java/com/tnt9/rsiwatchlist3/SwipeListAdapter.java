package com.tnt9.rsiwatchlist3;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SwipeListAdapter extends RecyclerView.Adapter<SwipeListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {


    private List<Stock> stocksList = new ArrayList<>();
    private Activity activity;

    public SwipeListAdapter(Activity activity, List<Stock> list) {
        this.activity = activity;
        stocksList = list;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_swipe, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {

        DecimalFormat decimalFormat = new DecimalFormat("0");
        holder.rsi.setText(decimalFormat.format(stocksList.get(position).getRsi()));

        holder.ticker.setText(stocksList.get(position).getTicker());
        holder.date.setText(stocksList.get(position).getDate());

        decimalFormat = new DecimalFormat("0.00");
        holder.rsiChange.setText(decimalFormat.format(stocksList.get(position).getRsiChange()));

        GradientDrawable rsiCircle = (GradientDrawable) holder.rsi.getBackground();
        int rsiColor = ContextCompat.getColor(activity, getRsiCircleColor(stocksList.get(position).getRsi()));
        rsiCircle.setColor(rsiColor);
    }

    @Override
    public int getItemCount() {
        return stocksList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(stocksList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return false;
    }

    @Override
    public void onItemDismiss(final int position) {

        final Stock stock = stocksList.get(position);

        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.main_container), stock.getTicker() + " removed from watchlist", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreItem(stock, position);
            }
        });
        snackbar.show();

        SQLiteOpenHelper stockListDatabaseHelper = new StockListDatabaseHelper(activity);
        SQLiteDatabase database = stockListDatabaseHelper.getWritableDatabase();
        database.delete("STOCKLIST", "TICKER = ?", new String[] {stocksList.get(position).getTicker()});

        stocksList.remove(position);
        notifyItemRemoved(position);

        for (int i = position; i < stocksList.size(); i++){
            stocksList.get(i).setOrder(i);

            ContentValues contentValues = new ContentValues();
            contentValues.put("ORDER_IN_LIST", i);
            database.update("STOCKLIST", contentValues, "TICKER =?", new String[] {stocksList.get(i).getTicker()});
        }
        database.close();
    }

    @Override
    public void restoreItem(Stock stock, int position) {

        SQLiteOpenHelper stockListDatabaseHelper = new StockListDatabaseHelper(activity);
        SQLiteDatabase database = stockListDatabaseHelper.getWritableDatabase();

        for (int i = position; i < stocksList.size(); i++){
            stocksList.get(i).setOrder(i + 1);

            ContentValues contentValues = new ContentValues();
            contentValues.put("ORDER_IN_LIST", i + 1);
            database.update("STOCKLIST", contentValues, "TICKER =?", new String[] {stocksList.get(i).getTicker()});
        }

        stocksList.add(position, stock);
        notifyItemInserted(position);

        ContentValues contentValues = new ContentValues();
        contentValues.put("TICKER", stock.getTicker());
        contentValues.put("ORDER_IN_LIST", stock.getOrder());
        contentValues.put("RSI", stock.getRsi());
        contentValues.put("DATE", stock.getDate());
        contentValues.put("RSI_CHANGE", stock.getRsiChange());
        database.insert("STOCKLIST", null, contentValues);
        database.close();
    }

    @Override
    public void addItem(Stock stock) {

        SQLiteOpenHelper stockListDatabaseHelper = new StockListDatabaseHelper(activity);
        SQLiteDatabase database = stockListDatabaseHelper.getWritableDatabase();

        Cursor cursor = database.query("STOCKLIST", new String[]{"TICKER"}, null, null, null, null, null); //TODO: Increase efficiency.
        int position = cursor.getCount();

        stock.setOrder(position);
        stocksList.add(stock);
        notifyItemInserted(position);

        ContentValues contentValues = new ContentValues();
        contentValues.put("TICKER", stock.getTicker());
        contentValues.put("ORDER_IN_LIST", stock.getOrder());
        contentValues.put("RSI", stock.getRsi());
        contentValues.put("DATE", stock.getDate());
        contentValues.put("RSI_CHANGE", stock.getRsiChange());
        database.insert("STOCKLIST", null, contentValues);

        cursor.close();
        database.close();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    private int getRsiCircleColor(double rsi) {
        int rsiColorResourceId;
        int rsiFloor = (int) Math.floor(rsi);

        if (rsiFloor > 100) rsiColorResourceId = R.color.rsi100;
        else if (rsiFloor > 90) rsiColorResourceId = R.color.rsi90;
        else if (rsiFloor > 80) rsiColorResourceId = R.color.rsi80;
        else if (rsiFloor > 70) rsiColorResourceId = R.color.rsi70;
        else if (rsiFloor > 60) rsiColorResourceId = R.color.rsi60;
        else if (rsiFloor > 50) rsiColorResourceId = R.color.rsi50;
        else if (rsiFloor > 40) rsiColorResourceId = R.color.rsi40;
        else if (rsiFloor > 30) rsiColorResourceId = R.color.rsi30;
        else if (rsiFloor > 20) rsiColorResourceId = R.color.rsi20;
        else rsiColorResourceId = R.color.rsi10;

        return rsiColorResourceId;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private final TextView rsi, ticker, date, rsiChange;
        private RelativeLayout viewBackground;
        private LinearLayout viewForeground;

        public ItemViewHolder(View itemView) {
            super(itemView);
            rsi = (TextView) itemView.findViewById(R.id.rsi_value);
            ticker = (TextView) itemView.findViewById(R.id.stock_ticker);
            date = (TextView) itemView.findViewById(R.id.date);
            rsiChange = (TextView) itemView.findViewById(R.id.rsi_change);
            viewBackground = itemView.findViewById(R.id.swipe_background);
            viewForeground = itemView.findViewById(R.id.swipe_foreground);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
            viewForeground.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
            viewForeground.setBackgroundColor(Color.WHITE);
        }
    }

}
