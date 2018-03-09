package com.tnt9.rsiwatchlist3;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public class DragAndDropFragment extends Fragment implements OnStartDragListener{

    private ItemTouchHelper itemTouchHelper;
    SwipeListFragment.ProgressBarListener progressBarListener;

    public DragAndDropFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        assert container != null;
        return new RecyclerView(container.getContext());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        progressBarListener = (SwipeListFragment.ProgressBarListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundColor(Color.WHITE);

        Bundle bundle = this.getArguments();
        List<Stock> listOfTickers = bundle.getParcelableArrayList(MainActivity.TAG);

        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DragAndDropAdapter dragAndDropAdapter = new DragAndDropAdapter(this, getActivity(), listOfTickers);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(dragAndDropAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(dragAndDropAdapter, true);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}
