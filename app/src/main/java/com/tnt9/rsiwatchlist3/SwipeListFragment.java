package com.tnt9.rsiwatchlist3;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SwipeListFragment extends Fragment implements OnStartDragListener{

    private List<Stock> listOfStockRefreshed;
    private List<Stock> listOfStocksAtStart;

    private int numberOfStockInDownloaded = 0;
    private SwipeListAdapter adapter;
    private OkHttpClient client;
    public static int numberOfStocks;
    private Handler mHandler;
    private String symbol;
    private double rsi;
    private String date;
    private double oldRsi;
    private ProgressBarListener progressBarListener;

    private static final String TAG = MainActivity.class.getName();

    public SwipeListFragment() {
    }

    public interface ProgressBarListener {
        void progressBarVisibility(int visibility);
        void refreshActive(boolean active);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        progressBarListener = (ProgressBarListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listOfStockRefreshed = new ArrayList<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean refreshAtStart = sharedPreferences.getBoolean("start_refresh_checkbox", false);
        if (refreshAtStart){
            client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS).build();
            //TODO: If time limit is reached, single stock appear in place of them multiple times

            try {
                SQLiteOpenHelper stockListDatabaseHelper = new StockListDatabaseHelper(getActivity());
                SQLiteDatabase database = stockListDatabaseHelper.getWritableDatabase();

                Cursor cursor = database.query("STOCKLIST", new String[]{"TICKER", "RSI", "DATE", "ORDER_IN_LIST", "RSI_CHANGE"}, null, null, null, null, null);
                numberOfStocks = cursor.getCount();

                progressBarListener.progressBarVisibility(View.VISIBLE);
                cursor.moveToFirst();

                downloadData(cursor.getString(0), cursor.getInt(1), cursor.getString(2), client, cursor.getInt(3), database);

                while (cursor.moveToNext()) {
                    downloadData(cursor.getString(0), cursor.getInt(1), cursor.getString(2), client, cursor.getInt(3), database);
                }
                cursor.close();

            } catch (SQLiteException e) {
                Toast.makeText(getActivity(), "Database unavailable", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.swipe_list_recycler, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        listOfStocksAtStart = new ArrayList<>();
        adapter = new SwipeListAdapter(getActivity(), listOfStocksAtStart);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter, false);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        try {
            SQLiteOpenHelper stockListDatabaseHelper = new StockListDatabaseHelper(getActivity());
            SQLiteDatabase database = stockListDatabaseHelper.getWritableDatabase();

            Cursor cursor = database.query("STOCKLIST", new String[]{"TICKER", "RSI", "DATE", "ORDER_IN_LIST", "RSI_CHANGE"}, null, null, null, null, null);

            numberOfStocks = cursor.getCount();

            cursor.moveToFirst();

            listOfStocksAtStart.add(new Stock(cursor.getString(0), cursor.getDouble(1), cursor.getInt(3) , cursor.getString(2), cursor.getDouble(4)));
            while (cursor.moveToNext()) {
                listOfStocksAtStart.add(new Stock(cursor.getString(0), cursor.getDouble(1), cursor.getInt(3) , cursor.getString(2), cursor.getDouble(4)));
            }
            Collections.sort(listOfStocksAtStart);
            recyclerView.setAdapter(adapter);

            cursor.close();
        } catch (SQLiteException e) {
            Toast.makeText(getActivity(), "Database unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadData(final String ticker, final int databaseRsi, final String databaseDate, OkHttpClient client, final int positionInList, final SQLiteDatabase database) {

        String url = "https://www.alphavantage.co/query?function=RSI&symbol=";
        String string = url + ticker + "&interval=" + "daily" + "&time_period=14&series_type=close&apikey=34C897W7DYVQPDYL";

        mHandler = new Handler(Looper.getMainLooper());

        Request request = new Request.Builder()
                .url(string)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {

                    String responseData = "";

                    try {

                        responseData = response.body().string();

                        JSONObject baseJsonResponse = new JSONObject(responseData);
                        JSONObject techAnalysis = baseJsonResponse.getJSONObject("Technical Analysis: RSI");

                        Iterator keys = techAnalysis.keys();

                        JSONObject currentRSI = techAnalysis.getJSONObject((String) keys.next());
                        rsi = currentRSI.getDouble("RSI");

                        JSONObject metaData = baseJsonResponse.getJSONObject("Meta Data");
                        symbol = metaData.getString("1: Symbol");
                        Log.v("OK", "----------------------");
                        date = metaData.getString("3: Last Refreshed");

                        JSONObject pastRSI = techAnalysis.getJSONObject((String) keys.next());
                        oldRsi = pastRSI.getDouble("RSI");

                        Log.v("DOWNLOAD", symbol + " | " + String.valueOf(rsi) + " | " + String.valueOf(positionInList) + " | " + date + "|" + String.valueOf(numberOfStockInDownloaded));
                        numberOfStockInDownloaded++;
                        listOfStockRefreshed.add(new Stock(symbol,(int) rsi, positionInList, date, rsi - oldRsi));

                        ContentValues contentValues = new ContentValues();
                        contentValues.put("RSI", rsi);
                        contentValues.put("DATE", date);
                        contentValues.put("RSI_CHANGE", rsi - oldRsi);
                        database.update("STOCKLIST", contentValues, "TICKER = ?",
                                new String[] {symbol});
//                        database.close();


                    } catch (JSONException e) {
                        //TODO:Handle JSON exception
                        Log.e(TAG, responseData, e);
                        rsi = databaseRsi;
                        date = databaseDate;
                        symbol = ticker;
                        numberOfStockInDownloaded++;
                        listOfStockRefreshed.add(new Stock(symbol,(int) rsi, positionInList, date, rsi - oldRsi));
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (numberOfStockInDownloaded == numberOfStocks) {
                                database.close();
                                Collections.sort(listOfStockRefreshed);

                                listOfStocksAtStart.clear();
                                listOfStocksAtStart.addAll(listOfStockRefreshed);

                                adapter.notifyDataSetChanged();
                                numberOfStockInDownloaded = 0;
                                progressBarListener.progressBarVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
    }

    public ArrayList<Stock> getListOfTickers(){
        ArrayList<Stock> listOfTickers = new ArrayList<>();
        listOfTickers.addAll(listOfStocksAtStart);
        return listOfTickers; //TODO: WTF?
    }

    public void addStock(final String ticker){

        for (int i = 0; i < listOfStocksAtStart.size(); i++){
            if (ticker.equals(listOfStocksAtStart.get(i).getTicker())) return;
        }

        String url = "https://www.alphavantage.co/query?function=RSI&symbol=";
        String string = url + ticker + "&interval=" + "daily" + "&time_period=14&series_type=close&apikey=34C897W7DYVQPDYL";

        final int POSITION_IN_LIST = numberOfStocks;

        mHandler = new Handler(Looper.getMainLooper());

        final Request request = new Request.Builder()
                .url(string)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {

                    String responseData = "";

                    try {

                        responseData = response.body().string();

                        JSONObject baseJsonResponse = new JSONObject(responseData);
                        JSONObject techAnalysis = baseJsonResponse.getJSONObject("Technical Analysis: RSI");

                        Iterator keys = techAnalysis.keys();

                        JSONObject currentRSI = techAnalysis.getJSONObject((String) keys.next());
                        rsi = currentRSI.getDouble("RSI");

                        JSONObject metaData = baseJsonResponse.getJSONObject("Meta Data");
                        symbol = metaData.getString("1: Symbol");
                        date = metaData.getString("3: Last Refreshed");

                        JSONObject pastRSI = techAnalysis.getJSONObject((String) keys.next());
                        oldRsi = pastRSI.getDouble("RSI");

                    } catch (JSONException e) {
                        //TODO:Handle JSON exception
                        Log.e(TAG, responseData, e);
                        symbol = ticker;
                        rsi = 0;
                        date = "NONE";
                        oldRsi = 0;
                        numberOfStockInDownloaded++;
                    }
                    Stock newStock = new Stock(symbol,(int) rsi, POSITION_IN_LIST, date, rsi - oldRsi);
                    adapter.addItem(newStock);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        listOfStockRefreshed.clear();
        listOfStocksAtStart.clear();
        super.onDestroyView();
    }
}
