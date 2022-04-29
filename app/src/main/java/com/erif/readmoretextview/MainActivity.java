package com.erif.readmoretextview;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextViewReadMore txtReadMore = findViewById(R.id.txtReadMore);
        txtReadMore.actionListener(new TextViewReadMore.Callback() {
            @Override
            public void onExpand() {
                //Toast.makeText(txtReadMore.getContext(), "Expand", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCollapse() {
                //Toast.makeText(txtReadMore.getContext(), "Collapse", Toast.LENGTH_SHORT).show();
            }
        });



        findViewById(R.id.btnSet).setOnClickListener(v -> {
            String lorem = getResources().getString(R.string.lorem_ipsum);
            String lorem1 = getResources().getString(R.string.lorem_ipsum3);
            String lorem2 = getResources().getString(R.string.lorem_ipsum4);
            i+=1;
            if (i == 1) {
                txtReadMore.setText(lorem1);
            } else if (i == 2) {
                txtReadMore.setText(lorem);
            }if (i == 3) {
                txtReadMore.setText(lorem2);
            }
        });

    }

    private void debug(String message) {
        Log.d("TextViewReadMore", message == null ? "null" : message);
    }

}