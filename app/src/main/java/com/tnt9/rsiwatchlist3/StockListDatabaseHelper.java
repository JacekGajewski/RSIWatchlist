package com.tnt9.rsiwatchlist3;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static java.sql.Types.INTEGER;

public class StockListDatabaseHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "stock_list";
    private static final int DB_VERSION = 1;
    public StockListDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE STOCKLIST (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TICKER TEXT, " + "ORDER_IN_LIST INTEGER, " + "RSI REAL, " + "RSI_CHANGE REAL, " + "DATE TEXT);");

        ContentValues contentValues = new ContentValues();
        contentValues.put("TICKER", "RSXJ");

        sqLiteDatabase.insert("STOCKLIST", null, contentValues);

        insertStock(sqLiteDatabase, "RSX", 1, 10, "2018-01-01", 0);
        insertStock(sqLiteDatabase, "GDXJ", 2, 15, "2018-01-01", 0);
        insertStock(sqLiteDatabase, "REMX", 3, 20, "2018-01-01", 0);
        insertStock(sqLiteDatabase, "OIH", 4, 25, "2018-01-01", 0);
        insertStock(sqLiteDatabase, "TUR", 5, 30, "2018-01-01", 0);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private static void insertStock(SQLiteDatabase database, String ticker, int orderInList, double rsi, String date, double rsiChange){

        ContentValues contentValues = new ContentValues();
        contentValues.put("TICKER", ticker);
        contentValues.put("ORDER_IN_LIST", orderInList);
        contentValues.put("RSI", rsi);
        contentValues.put("DATE", date);
        contentValues.put("RSI_CHANGE", rsiChange);
        database.insert("STOCKLIST", null, contentValues);
    }
}
