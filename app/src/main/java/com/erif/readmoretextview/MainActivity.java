package com.erif.readmoretextview;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private int i = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextViewReadMore txtReadMore = findViewById(R.id.txtReadMore);
        txtReadMore.onClickExpand(v -> txtReadMore.toggle());
        txtReadMore.onClickCollapse(v -> txtReadMore.toggle());
        txtReadMore.toggleListener(collapsed -> {

        });

        findViewById(R.id.btnSet).setOnClickListener(v -> {
            String lorem = getResources().getString(R.string.lorem_ipsum);
            String lorem1 = getResources().getString(R.string.lorem_ipsum1);
            String lorem2 = getResources().getString(R.string.lorem_ipsum2);
            String lorem3 = getResources().getString(R.string.lorem_ipsum3);
            String lorem4 = getResources().getString(R.string.lorem_ipsum4);
            if (i < 4)
                i+=1;
            else
                i=0;
            if (i == 0) {
                txtReadMore.setText(lorem);
            } else if (i == 1) {
                txtReadMore.setText(lorem1);
            } else if (i == 2) {
                txtReadMore.setText(lorem2);
            } else if (i == 3) {
                txtReadMore.setText(lorem3);
            } else if (i == 4) {
                txtReadMore.setText(lorem4);
            }
        });

        findViewById(R.id.act_main_fab).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivityRecyclerView.class);
            startActivity(intent);
        });

    }

    private void debug(String message) {
        Log.d("TextViewReadMore", message);
    }

}