package com.kutira.kone.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kutira.kone.R;
import com.kutira.kone.adapters.ScrapAdapter;
import com.kutira.kone.models.FabricScrap;
import com.kutira.kone.models.User;
import com.kutira.kone.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;
    private List<FabricScrap> myScrapList;
    private ScrapAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseHelper = FirebaseHelper.getInstance();

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        myScrapList = new ArrayList<>();
        adapter = new ScrapAdapter(myScrapList, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        loadProfile();
        loadMyListings();
    }

    private void loadProfile() {
        String uid = firebaseHelper.getCurrentUserId();
        firebaseHelper.getFirestore().collection(FirebaseHelper.COLLECTION_USERS)
                .document(uid).get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        tvName.setText(user.getName());
                        tvEmail.setText(user.getEmail());
                        tvPhone.setText(user.getPhone());
                    }
                });
    }

    private void loadMyListings() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = firebaseHelper.getCurrentUserId();
        firebaseHelper.getFirestore().collection(FirebaseHelper.COLLECTION_SCRAPS)
                .whereEqualTo("ownerId", uid)
                .get()
                .addOnSuccessListener(snapshots -> {
                    progressBar.setVisibility(View.GONE);
                    myScrapList.clear();
                    for (var doc : snapshots.getDocuments()) {
                        FabricScrap scrap = doc.toObject(FabricScrap.class);
                        if (scrap != null) {
                            scrap.setId(doc.getId());
                            myScrapList.add(scrap);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
