package com.tnt9.rsiwatchlist3;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.DecimalFormat;

public class Stock implements Comparable<Stock>, Parcelable{

    private double rsi;
    private String ticker;
    private int order;



    private double rsiChange;
    private String date;


    public Stock(String ticker, double rsi, int order, String date, double rsiChange) {
        this.ticker = ticker;
        this.rsi = rsi;
        this.order = order;
        this.date = date; // TODO: Adjust date to time zone.
        this.rsiChange = rsiChange;
    }

    public Stock(Parcel parcel){
        this.ticker = parcel.readString();
        this.rsi = parcel.readInt();
        this.order = parcel.readInt();
        this.date = parcel.readString();
    }


    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public double getRsi() {
        return rsi;
    }

    public void setRsi(int rsi) {
        this.rsi = rsi;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }


    public String getDate() {
        return date;
    }

    public double getRsiChange() {
        return rsiChange;
    }

    public void setRsiChange(double rsiChange) {
        this.rsiChange = rsiChange;
    }

    @Override
    public int compareTo(@NonNull Stock stock) {

        if (order > stock.getOrder()){
            return 1;
        }
        return -1;
    }

    public static final Parcelable.Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel parcel) {
            return new Stock(parcel);
        }

        @Override
        public Object[] newArray(int size) {
            return new Stock[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.ticker);
        parcel.writeDouble(this.rsi);
        parcel.writeInt(this.order);
        parcel.writeString(this.date);
    }
}
