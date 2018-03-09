package com.tnt9.rsiwatchlist3;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CheckRsiService extends Service{

    private static final String TAG = CheckRsiService.class.getName();
    int numberOfStocksInDatabase;
    int numberOfStocksRefreshed = 0;
    int maxRsi;
    int minRsi;
    Boolean notifyTop;
    Boolean notifyBottom;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT >= 26){
            Notification notification = new NotificationCompat.Builder(this, getResources().getString(R.string.app_name))
                    .setContentTitle("RSIWatch")
                    .build();
            startForeground(0, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        notifyTop = sharedPreferences.getBoolean("notifications_max_checkbox", false);
        notifyBottom = sharedPreferences.getBoolean("notifications_min_checkbox", false);
        maxRsi = Integer.valueOf(sharedPreferences.getString("max_rsi", "100"));
        minRsi = Integer.valueOf(sharedPreferences.getString("min_rsi", "0"));

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS).build();

        //TODO: If time limit is reached, single stock appear in place of them multiple times

        try {

            SQLiteOpenHelper stockListDatabaseHelper = new StockListDatabaseHelper(getApplicationContext());
            SQLiteDatabase database = stockListDatabaseHelper.getWritableDatabase();

            Cursor cursor = database.query("STOCKLIST", new String[]{"TICKER", "RSI", "DATE", "ORDER_IN_LIST", "RSI_CHANGE"}, null, null, null, null, null);
            numberOfStocksInDatabase = cursor.getCount();

            cursor.moveToFirst();
            downloadData(cursor.getString(0), client, database);

            while (cursor.moveToNext()) {
                downloadData(cursor.getString(0), client, database);
            }

            cursor.close();

        } catch (SQLiteException e) {
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_LONG).show();
            Log.e(TAG, "onStartCommand: ", e);
        }
        return Service.START_STICKY;
    }


    private double rsi;
    private double oldRsi;
    private String symbol;
    private String date;


    public void downloadData(final String ticker, OkHttpClient client, final SQLiteDatabase database){
        String url = "https://www.alphavantage.co/query?function=RSI&symbol=";
        String string = url + ticker + "&interval=" + "daily" + "&time_period=14&series_type=close&apikey=34C897W7DYVQPDYL";

        final Handler mHandler = new Handler(Looper.getMainLooper());

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
                        date = metaData.getString("3: Last Refreshed");

                        JSONObject pastRSI = techAnalysis.getJSONObject((String) keys.next());
                        oldRsi = pastRSI.getDouble("RSI");

                        numberOfStocksRefreshed++;

                        ContentValues contentValues = new ContentValues();
                        contentValues.put("RSI", rsi);
                        contentValues.put("DATE", date);
                        contentValues.put("RSI_CHANGE", rsi - oldRsi);
                        database.update("STOCKLIST", contentValues, "TICKER = ?",
                                new String[] {symbol});

                        if ((rsi < minRsi && notifyBottom) || (rsi > maxRsi & notifyTop)){
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            final PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                            Notification n  = new Notification.Builder(getApplicationContext())
                                    .setContentTitle(symbol)
                                    .setContentText("RSI: " + String.valueOf(rsi))
                                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                                    .setContentIntent(pIntent)
                                    .setAutoCancel(true).build();

                            NotificationManager notificationManager =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            assert notificationManager != null;
                            notificationManager.notify(0, n);
                        }


                    } catch (JSONException e) {
                        numberOfStocksRefreshed++;
                        Log.e(TAG, responseData, e);
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (numberOfStocksRefreshed == numberOfStocksInDatabase) {
                                numberOfStocksRefreshed = 0;
                                database.close();
                            }
                        }
                    });
                }
            }
        });
    }
}
