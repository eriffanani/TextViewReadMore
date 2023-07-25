package com.erif.readmoretextview;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ActDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Another Detail Example");
        }

        TextViewReadMore txtEntered = findViewById(R.id.act_detail_txtEntered);
        txtEntered.onClickExpand(v -> txtEntered.toggle());
        txtEntered.onClickCollapse(v -> txtEntered.toggle());

        TextViewReadMore txtAnim = findViewById(R.id.act_detail_txtAnim);
        txtAnim.onClickExpand(v -> txtAnim.toggle());
        txtAnim.onClickCollapse(v -> txtAnim.toggle());

        TextViewReadMore txtJustify = findViewById(R.id.act_detail_txtJustify);
        txtJustify.onClickExpand(v -> txtJustify.toggle());
        txtJustify.onClickCollapse(v -> txtJustify.toggle());

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
