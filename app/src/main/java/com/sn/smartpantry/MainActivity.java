package com.sn.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sn.smartpantry.data.PantryRepository;
import com.sn.smartpantry.data.entity.PantryItem;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private PantryRepository repository;
    private PantryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new PantryRepository(getApplication());

        RecyclerView recyclerView = findViewById(R.id.pantryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        recyclerView.setAdapter(adapter);

        View emptyText = findViewById(R.id.emptyText);

        repository.getAllItems().observe(this, items -> {
            adapter.submitList(items);
            emptyText.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        findViewById(R.id.fabAddItem).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditPantryActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onItemClicked(PantryItem item) {
        Intent intent = new Intent(this, AddEditPantryActivity.class);
        intent.putExtra("item_id", item.id);
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(PantryItem item) {
        repository.delete(item);
    }
}
