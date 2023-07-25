package com.erif.readmoretextview;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.erif.readmoretextview.adapter.AdapterRecyclerView;
import com.erif.readmoretextview.model.ModelItemRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private CoordinatorLayout parentView;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private AdapterRecyclerView adapter;
    private final List<ModelItemRecyclerView> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);
        parentView = findViewById(R.id.act_main_parentView);
        toolbar = findViewById(R.id.act_main_toolbar);
        setSupportActionBar(toolbar);
        adapter = new AdapterRecyclerView();

        recyclerView = findViewById(R.id.act_main_recyclerView);

        recyclerView.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        SimpleItemAnimator animator = (SimpleItemAnimator) recyclerView.getItemAnimator();
        if (animator != null)
            animator.setSupportsChangeAnimations(false);

        String[] arr = new String[] {
                getString(R.string.lorem_ipsum1),
                getString(R.string.lorem_short),
                getString(R.string.lorem_ipsum2),
                getString(R.string.lorem_ipsum3),
                "Short text 1",
                getString(R.string.lorem_ipsum4),
                getString(R.string.lorem_ipsum1),
                getString(R.string.lorem_ipsum2),
                "Short text 2",
                getString(R.string.lorem_ipsum1),
                getString(R.string.lorem_ipsum2)
        };

        String[] arrName = new String[] {
                "John Doe", "Louise Bourgeois",
                "Rafael Nadal", "Maria Sharapova",
                "C. Ronaldo", "Serena Williams",
                "John Doe", "Louise Bourgeois",
                "Rafael Nadal", "Maria Sharapova",
                "C. Ronaldo", "Serena Williams"
        };

        int [] profile = new int[] {
                R.mipmap.man1,
                R.mipmap.women1,
                R.mipmap.man2,
                R.mipmap.women2,
                R.mipmap.man3,
                R.mipmap.women3,
                R.mipmap.man1,
                R.mipmap.women1,
                R.mipmap.man2,
                R.mipmap.women2,
                R.mipmap.man3,
                R.mipmap.women3
        };

        int [] img = new int[] {
                R.mipmap.img2,
                R.mipmap.img3,
                R.mipmap.img4,
                R.mipmap.img5,
                R.mipmap.img2,
                R.mipmap.img3,
                R.mipmap.img4,
                R.mipmap.img5,
                R.mipmap.img2,
                R.mipmap.img3,
                R.mipmap.img4
        };

        for (int i=0; i<arr.length; i++) {
            ModelItemRecyclerView item = new ModelItemRecyclerView(
                    i, arrName[i], profile[i], img[i], arr[i]
            );
            item.setShowImage(new Random().nextBoolean());
            if (i == 3) {
                item.setCollapsed(false);
            }
            list.add(item);
        }
        applyWindows();
    }

    private void applyWindows() {
        ViewCompat.setOnApplyWindowInsetsListener(parentView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int statusBarHeight = systemBars.top;
            int navigationBarHeight = systemBars.bottom;
            ViewGroup.MarginLayoutParams toolbarParam = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            toolbarParam.topMargin = statusBarHeight;
            toolbar.post(() -> {
                int toolbarHeight = toolbar.getHeight();
                int recyclerViewPaddingTop = statusBarHeight + toolbarHeight;
                recyclerView.setPadding(
                        recyclerView.getPaddingStart(), recyclerViewPaddingTop,
                        recyclerView.getPaddingEnd(), navigationBarHeight
                );
                adapter.setList(list);
            });
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.main_menu_short_entered) {
            Intent intent = new Intent(this, ActDetail.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}