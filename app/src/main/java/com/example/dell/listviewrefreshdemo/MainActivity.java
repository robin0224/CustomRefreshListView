package com.example.dell.listviewrefreshdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import com.example.view.RefreshListView;
import com.example.view.RefreshListener;

public class MainActivity extends AppCompatActivity implements RefreshListener{

    Handler handler;
    RefreshListView listView;
    ArrayAdapter adapter;
    int refreshIndex;
    String str[] = {"item1", "item2", "item3", "item4", "item5","item6", "item7", "item8", "item9", "item10","item11", "item12", "item13", "item14", "item15"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        listView = (RefreshListView)findViewById(R.id.listView);
        listView.setListener(this);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, str);
        listView.setAdapter(adapter);

    }

    @Override
    public void onRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(refreshIndex >= str.length){
                    refreshIndex = 0;
                }
                str[refreshIndex] = "refresh " + str[refreshIndex];
                refreshIndex ++;
                adapter.notifyDataSetChanged();
                listView.refreshComplete();
            }
        }, 1000);
    }
}
