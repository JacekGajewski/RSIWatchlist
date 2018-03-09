package com.tnt9.rsiwatchlist3;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DragAndDropAdapter  extends RecyclerView.Adapter<DragAndDropAdapter.ItemViewHolder> implements ItemTouchHelperAdapter{

    private List<Stock> stocks = new ArrayList<>();
    private final OnStartDragListener dragListener;
    private Activity activity;

    public DragAndDropAdapter(OnStartDragListener onStartDragListener, Activity activity, List<Stock> stocks) {
        dragListener = onStartDragListener;
        this.activity = activity;
        this.stocks = stocks;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drag_drop, parent, false);
        return new ItemViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.ticker.setText(stocks.get(position).getTicker());

        DecimalFormat decimalFormat = new DecimalFormat("0");
        holder.rsi.setText(decimalFormat.format(stocks.get(position).getRsi()));

        GradientDrawable rsiCircle = (GradientDrawable) holder.rsi.getBackground();
        int rsiColor = ContextCompat.getColor(activity, getRsiCircleColor(stocks.get(position).getRsi()));
        rsiCircle.setColor(rsiColor);

        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
                    dragListener.onStartDrag(holder);
                return false;
            }

        });
    }


    @Override
    public int getItemCount() {
        return stocks.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        SQLiteOpenHelper stockListDatabaseHelper = new StockListDatabaseHelper(activity);
        SQLiteDatabase database = stockListDatabaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("ORDER_IN_LIST", toPosition);
        database.update("STOCKLIST", contentValues, "TICKER =?", new String[] {stocks.get(fromPosition).getTicker()});

        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("ORDER_IN_LIST", fromPosition);
        database.update("STOCKLIST", contentValues2, "TICKER =?", new String[] {stocks.get(toPosition).getTicker()});

        database.close();

        stocks.get(fromPosition).setOrder(toPosition);
        stocks.get(toPosition).setOrder(fromPosition);
        Collections.swap(stocks, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
    }

    @Override
    public void restoreItem(Stock stock, int position) {
    }

    @Override
    public void addItem(Stock stock) {
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


    public class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder{

        public final TextView ticker, rsi;
        public final ImageView handleView;
        public final RelativeLayout viewForeground;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ticker = (TextView) itemView.findViewById(R.id.drag_stock_ticker);
            rsi = (TextView) itemView.findViewById(R.id.drag_rsi_value);
            handleView = (ImageView) itemView.findViewById(R.id.handle);
            viewForeground = itemView.findViewById(R.id.view_foreground);
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
