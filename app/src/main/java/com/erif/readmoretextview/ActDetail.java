package com.erif.readmoretextview;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.erif.readmoretextview.adapter.AdapterRecyclerView;
import com.erif.readmoretextview.model.ModelItemRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ActDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Entered Text");
        }

        TextViewReadMore txtEntered = findViewById(R.id.act_detail_txtEntered);
        txtEntered.onClickExpand(v -> {
            txtEntered.toggle();
        });
        txtEntered.onClickCollapse(v -> {
            txtEntered.toggle();
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
