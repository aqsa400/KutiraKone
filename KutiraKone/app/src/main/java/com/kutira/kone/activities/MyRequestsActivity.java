package com.kutira.kone.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kutira.kone.R;
import com.kutira.kone.adapters.RequestAdapter;
import com.kutira.kone.models.SwapRequest;
import com.kutira.kone.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private RecyclerView rvSent, rvReceived;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;
    private List<SwapRequest> sentList, receivedList;
    private RequestAdapter sentAdapter, receivedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseHelper = FirebaseHelper.getInstance();

        rvSent = findViewById(R.id.rvSent);
        rvReceived = findViewById(R.id.rvReceived);
        progressBar = findViewById(R.id.progressBar);

        sentList = new ArrayList<>();
        receivedList = new ArrayList<>();
        sentAdapter = new RequestAdapter(sentList, this, false);
        receivedAdapter = new RequestAdapter(receivedList, this, true);

        rvSent.setLayoutManager(new LinearLayoutManager(this));
        rvReceived.setLayoutManager(new LinearLayoutManager(this));
        rvSent.setAdapter(sentAdapter);
        rvReceived.setAdapter(receivedAdapter);

        loadRequests();
    }

    private void loadRequests() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = firebaseHelper.getCurrentUserId();

        // Load sent requests
        firebaseHelper.getFirestore().collection(FirebaseHelper.COLLECTION_REQUESTS)
                .whereEqualTo("requesterId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    sentList.clear();
                    for (var doc : snap.getDocuments()) {
                        SwapRequest req = doc.toObject(SwapRequest.class);
                        if (req != null) { req.setId(doc.getId()); sentList.add(req); }
                    }
                    sentAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });

        // Load received requests
        firebaseHelper.getFirestore().collection(FirebaseHelper.COLLECTION_REQUESTS)
                .whereEqualTo("ownerId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    receivedList.clear();
                    for (var doc : snap.getDocuments()) {
                        SwapRequest req = doc.toObject(SwapRequest.class);
                        if (req != null) { req.setId(doc.getId()); receivedList.add(req); }
                    }
                    receivedAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
