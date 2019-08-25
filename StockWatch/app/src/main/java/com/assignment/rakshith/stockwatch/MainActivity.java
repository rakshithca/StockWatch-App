package com.assignment.rakshith.stockwatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements OnClickListener, View.OnLongClickListener {

    private RecyclerView RecyclerV;
    private RecyclerView.LayoutManager LayoutMngr;
    private AdapterStock mAdpt;
    private SwipeRefreshLayout SwipeRefresh;
    private List<Stock> stockList = new ArrayList<>();
    private static final String TAG = "MainActivity";
    private HashMap<String, String> Stock_data = new HashMap<>();
    private StockDatabase DatabaseStck;
    ArrayList<String[]> dbstck = new ArrayList<>();

    private final String Market_watch_link = "https://www.marketwatch.com/investing/stock/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerV = findViewById(R.id.recyclerview_stock);
        SwipeRefresh = findViewById(R.id.swiperefresh);

        RecyclerV.setHasFixedSize(true);

        LayoutMngr = new LinearLayoutManager(this);
        RecyclerV.setLayoutManager(LayoutMngr);

        mAdpt = new AdapterStock(stockList, this);
        RecyclerV.setAdapter(mAdpt);


        DownloadName nd = new DownloadName(this);
        nd.execute();

        DatabaseStck = new StockDatabase(this);
        dbstck = DatabaseStck.loadStocks();

        if (isNetworkAvailable()) {
            for (int i = 0; i < dbstck.size(); i++) {
                DownloadStock sd = new DownloadStock(this);
                sd.execute(dbstck.get(i)[0]);
            }
        } else {
            for (int i = 0; i < dbstck.size(); i++) {
                Stock s = new Stock(dbstck.get(i)[0], dbstck.get(i)[1], 0.0, 0.0, 0.0);
                stockList.add(s);
            }
            Collections.sort(stockList, new Comparator<Stock>() {
                @Override
                public int compare(Stock o1, Stock o2) {
                    return o1.getStockSymbol().compareTo(o2.getStockSymbol());
                }
            });
            mAdpt.notifyDataSetChanged();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Updated Without A Network Connection");
            AlertDialog alert = builder.create();
            alert.show();
        }

        SwipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.d(TAG, "onRefresh: Refresh Called");
                        Toast.makeText(getApplicationContext(), "Refreshing..", Toast.LENGTH_SHORT).show();
                        SwipeRefresh.setRefreshing(false);
                        loadStocksFromDB();
                    }

                });
    }

    public void loadStocksFromDB() {
        DatabaseStck = new StockDatabase(this);
        dbstck = DatabaseStck.loadStocks();
        if (isNetworkAvailable()) {
            stockList.clear();
            for (int i = 0; i < dbstck.size(); i++) {
                DownloadStock sd = new DownloadStock(this);
                sd.execute(dbstck.get(i)[0]);
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Updated Without A Network Connection");
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void getStockSymbolData(HashMap stockData) {
        Stock_data = stockData;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.d(TAG, "isNetworkAvailable: Cannot Access Connectivity Manger");
            return false;
        }
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Log.d(TAG, "isNetworkAvailable: Connected to the Internet");
            DownloadName alt = new DownloadName(this);
            alt.execute();
            return true;
        } else {
            Log.d(TAG, "isNetworkAvailable: Connected to the Internet");
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opt_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.addStock:
                if (isNetworkAvailable()) {
                    Toast.makeText(this, "Add Stock pressed", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.add_stock, null);
                    builder.setView(dialogView);

                    final EditText editText = (EditText) dialogView.findViewById(R.id.editTextAddStock);
                    editText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

                    builder.setTitle("Stock Selection");
                    builder.setMessage("Please enter a Stock Symbol:");

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ArrayList<String> results = new ArrayList<String>();
                            String searchedStockSymbol = editText.getText().toString().trim();
                            results = searchStock(searchedStockSymbol);
                            if (results == null || results.size() == 0) {
                                //No matching stock
                                dialogNoMatchStock(searchedStockSymbol);
                            } else if (results.size() == 1) {
                                //One stock matching
                                DownloadStock alt = new DownloadStock(MainActivity.this);
                                alt.execute(results.get(0).toString());
                            } else {
                                //Multiple stock matching
                                dialogMutipleStock(results);
                            }
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("No Network Connection");
                    builder.setMessage("Stocks Cannot Be Added Without A Network Connection");

                    AlertDialog alert = builder.create();
                    alert.show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<String> searchStock(String regex) {

        ArrayList<String> stocks = new ArrayList<String>();
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Iterator<String> keysIterator = Stock_data.keySet().iterator();
        while (keysIterator.hasNext()) {
            String candidate = keysIterator.next();
            Matcher m = p.matcher(candidate);
            if (m.find()) {
                System.out.println("it matches" + candidate);
                stocks.add(candidate);
            }
        }
        return stocks;
    }

    private void dialogNoMatchStock(String stock) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Symbol Not Found: " + stock);
        builder.setMessage("Data for stock symbol");
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void dialogMutipleStock(ArrayList<String> stocks) {
        Toast.makeText(this, "Dialog multiple stocks", Toast.LENGTH_SHORT);

        final ArrayAdapter<String> multipleStocksMenu = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);

        for (int i = 0; i < stocks.size(); i++) {
            multipleStocksMenu.add(stocks.get(i) + "-" + Stock_data.get(stocks.get(i)));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final DownloadStock sd = new DownloadStock(this);


        builder.setAdapter(multipleStocksMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String s = multipleStocksMenu.getItem(which);
                String parts[] = s.split("-", 2);
                sd.execute(parts[0]);
            }
        });

        builder.setTitle("Make a selection");
        builder.setNegativeButton("NEVERMIND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onClick(View v) {
        int position = RecyclerV.getChildLayoutPosition(v);
        Stock stock = stockList.get(position);
        Toast.makeText(getApplicationContext(), "Selected: " + stock.getStockSymbol(), Toast.LENGTH_SHORT).show();

        Intent webBrowserIntent = new Intent(Intent.ACTION_VIEW);
        webBrowserIntent.setData(Uri.parse(Market_watch_link + stock.getStockSymbol()));
        startActivity(webBrowserIntent);
    }

    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "onLongClick: Pressed");
        Toast.makeText(this, "Long Click pressed", Toast.LENGTH_LONG);

        final int position = RecyclerV.getChildLayoutPosition(v);
        Stock s = stockList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(this.getResources().getDrawable(R.drawable.baseline_delete_black_24));
        builder.setTitle("Delete Stock");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseStck.deleteStock(stockList.get(position).getStockSymbol());
                stockList.remove(position);
                mAdpt.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setMessage("Delete Stock " + stockList.get(position).getStockSymbol() + "?");
        builder.setTitle("Delete Stock");

        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    public void updateStockList(Stock stock) {
        boolean duplicateFlag = false;


        for (int i = 0; i < stockList.size(); i++) {
            if (stockList.get(i).getStockSymbol().equalsIgnoreCase(stock.getStockSymbol())) {
                duplicateFlag = true;
                break;
            }
        }
        if (duplicateFlag) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(this.getResources().getDrawable(R.drawable.baseline_warning_black_24));
            builder.setTitle("Duplicate Stock");
            builder.setMessage("Stock Symbol " + stock.getStockSymbol() + " is already displayed");

            AlertDialog alert = builder.create();
            alert.show();
        } else {
            stockList.add(stock);
            Collections.sort(stockList, new Comparator<Stock>() {
                @Override
                public int compare(Stock o1, Stock o2) {
                    return o1.getStockSymbol().compareTo(o2.getStockSymbol());
                }
            });
            mAdpt.notifyDataSetChanged();
            DatabaseStck.addStock(stock);
        }
    }


}


