package com.erif.readmoretextview;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.erif.readmoretextview.helper.AdapterRecyclerView;
import com.erif.readmoretextview.helper.ModelItemRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivityRecyclerView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_recycler_view);
        AdapterRecyclerView adapter = new AdapterRecyclerView();

        RecyclerView recyclerView = findViewById(R.id.act_main_recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String[] arr = new String[]{
                getString(R.string.lorem_ipsum1),
                getString(R.string.lorem_ipsum2),
                getString(R.string.lorem_ipsum3),
                getString(R.string.lorem_ipsum1),
                getString(R.string.lorem_ipsum2),
                getString(R.string.lorem_ipsum3),
                getString(R.string.lorem_ipsum1),
                getString(R.string.lorem_ipsum2),
                getString(R.string.lorem_ipsum3),
                getString(R.string.lorem_ipsum1),
                getString(R.string.lorem_ipsum2),
                getString(R.string.lorem_ipsum3),
                getString(R.string.lorem_ipsum1),
                getString(R.string.lorem_ipsum2),
                getString(R.string.lorem_ipsum3)
        };
        List<ModelItemRecyclerView> list = new ArrayList<>();
        for (int i=0; i<arr.length; i++) {
            list.add(new ModelItemRecyclerView(i, arr[i]));
        }
        adapter.setList(list);
        recyclerView.setItemViewCacheSize(arr.length);

    }
}