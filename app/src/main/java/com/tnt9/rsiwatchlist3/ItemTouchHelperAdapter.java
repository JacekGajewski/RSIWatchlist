package com.tnt9.rsiwatchlist3;

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

    void restoreItem(Stock stock, int position);

    void addItem(Stock stock);

}
