package com.github.rubensousa.recyclerviewsnap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.Gravity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView startRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewStart);
        RecyclerView topRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewTop);
        RecyclerView endRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewEnd);
        RecyclerView bottomRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewBottom);

        Adapter adapterHorizontal = new Adapter(true);
        Adapter adapterVertical = new Adapter(false);
        setupAdapter(adapterHorizontal);
        setupAdapter(adapterVertical);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false));

        startRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));

        topRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        endRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));

        bottomRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapterHorizontal);
        startRecyclerView.setAdapter(adapterHorizontal);
        topRecyclerView.setAdapter(adapterVertical);
        endRecyclerView.setAdapter(adapterHorizontal);
        bottomRecyclerView.setAdapter(adapterVertical);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        SnapHelper snapHelperStart = new GravitySnapHelper(Gravity.START);
        snapHelperStart.attachToRecyclerView(startRecyclerView);

        SnapHelper snapHelperTop = new GravitySnapHelper(Gravity.TOP);
        snapHelperTop.attachToRecyclerView(topRecyclerView);

        SnapHelper snapHelperEnd = new GravitySnapHelper(Gravity.END);
        snapHelperEnd.attachToRecyclerView(endRecyclerView);

        SnapHelper snapHelperBottom = new GravitySnapHelper(Gravity.BOTTOM);
        snapHelperBottom.attachToRecyclerView(bottomRecyclerView);
    }

    private void setupAdapter(Adapter adapter) {
        adapter.addApp(new App("Google+", R.drawable.ic_google_48dp, 4.6f));
        adapter.addApp(new App("Gmail", R.drawable.ic_gmail_48dp, 4.8f));
        adapter.addApp(new App("Inbox", R.drawable.ic_inbox_48dp, 4.5f));
        adapter.addApp(new App("Google Keep", R.drawable.ic_keep_48dp, 4.2f));
        adapter.addApp(new App("Google Drive", R.drawable.ic_drive_48dp, 4.6f));
        adapter.addApp(new App("Hangouts", R.drawable.ic_hangouts_48dp, 3.9f));
        adapter.addApp(new App("Google Photos", R.drawable.ic_photos_48dp, 4.6f));
        adapter.addApp(new App("Messenger", R.drawable.ic_messenger_48dp, 4.2f));
        adapter.addApp(new App("Sheets", R.drawable.ic_sheets_48dp, 4.2f));
        adapter.addApp(new App("Slides", R.drawable.ic_slides_48dp, 4.2f));
        adapter.addApp(new App("Docs", R.drawable.ic_docs_48dp, 4.2f));
    }
}
