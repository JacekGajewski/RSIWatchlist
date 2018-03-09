package com.tnt9.rsiwatchlist3;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SwipeListFragment.ProgressBarListener {

    CustomSwipeRefreshLayout swipeContainer;
    private MaterialSearchView searchView;

    private CustomBroadcastReceiver customBroadcastReceiver;
    public static final String TAG = "MainActivity";

    // 1) OnUpgrade Database, version check.
    // 2) Deleting positions during loading.
    // 3) Check if ticker even exists.
    // 4) Add about.
    // 5) Interday?
    // 6) Set time of background refresh.
    // 7) Screen rotation.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        swipeContainer = (CustomSwipeRefreshLayout) findViewById(R.id.swipe_container);

        setupSearchView();

        Toolbar toolbar = findViewById(R.id.toolbar) ;
        setSupportActionBar(toolbar);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.content);

                if (currentFragment instanceof SwipeListFragment){
                    SwipeListFragment fragment = new SwipeListFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.detach(fragment).attach(fragment).commitAllowingStateLoss();
                }
            }
        });

        if (savedInstanceState == null) {
            startFragment(new SwipeListFragment());
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Fragment fragment = getFragmentManager().findFragmentById(R.id.content);
                if (fragment instanceof SwipeListFragment) {
                    ((SwipeListFragment) fragment).addStock(query.toUpperCase());
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

            }
        });

        searchView.setHint("Ticker");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.action_order:

                Fragment fragment = getFragmentManager().findFragmentById(R.id.content);

                if (fragment instanceof SwipeListFragment &&  !swipeContainer.isRefreshing()){
                    ArrayList<Stock> stocksTicker =((SwipeListFragment) fragment).getListOfTickers();
                    refreshActive(false);

                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(TAG, stocksTicker);

                    Fragment dragAndDropFragment = new DragAndDropFragment();
                    dragAndDropFragment.setArguments(bundle);

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content, dragAndDropFragment)
                            .addToBackStack(null)
                            .commit();
                }
                return true;

            case R.id.action_settings:
                refreshActive(false);
                startFragment(new SettingsFragment());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.content);

        if (fragment instanceof DragAndDropFragment){
            refreshActive(true);
            super.onBackPressed();

        }else if ((fragment instanceof SwipeListFragment)){
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);

        }else if (fragment instanceof SettingsFragment){
            refreshActive(true);
            startFragment(new SwipeListFragment());
        }
    }

    private void startFragment(Fragment fragment){
        getFragmentManager().beginTransaction()
                .add(R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    // visibility = 4 - INVISIBLE
    // visibility = 0 - VISIBLE
    @Override
    public void progressBarVisibility(int visibility) {
        //findViewById(R.id.progress_bar).setVisibility(visibility);
        if (visibility == 4) swipeContainer.setRefreshing(false);
        else swipeContainer.setRefreshing(true);
    }

    @Override
    public void refreshActive(boolean active) {
        swipeContainer.setEnabled(active);
    }


    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        customBroadcastReceiver = new CustomBroadcastReceiver();
        registerReceiver(customBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(customBroadcastReceiver);
    }
}
