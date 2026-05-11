package com.kutira.kone.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.kutira.kone.R;
import com.kutira.kone.models.FabricScrap;
import com.kutira.kone.models.SwapRequest;
import com.kutira.kone.models.User;
import com.kutira.kone.utils.FirebaseHelper;
import com.kutira.kone.utils.LocationHelper;

public class ScrapDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SCRAP_ID = "scrap_id";

    private ImageView ivImage;
    private TextView tvTitle, tvMaterial, tvSize, tvOwner, tvLocation, tvDistance, tvDescription;
    private Button btnRequest;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;
    private FabricScrap currentScrap;
    private double userLat = 0, userLon = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrap_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseHelper = FirebaseHelper.getInstance();

        ivImage = findViewById(R.id.ivImage);
        tvTitle = findViewById(R.id.tvTitle);
        tvMaterial = findViewById(R.id.tvMaterial);
        tvSize = findViewById(R.id.tvSize);
        tvOwner = findViewById(R.id.tvOwner);
        tvLocation = findViewById(R.id.tvLocation);
        tvDistance = findViewById(R.id.tvDistance);
        tvDescription = findViewById(R.id.tvDescription);
        btnRequest = findViewById(R.id.btnRequest);
        progressBar = findViewById(R.id.progressBar);

        String scrapId = getIntent().getStringExtra(EXTRA_SCRAP_ID);
        if (scrapId != null) loadScrap(scrapId);

        btnRequest.setOnClickListener(v -> showRequestDialog());
    }

    private void loadScrap(String scrapId) {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getFirestore()
                .collection(FirebaseHelper.COLLECTION_SCRAPS)
                .document(scrapId)
                .get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);
                    currentScrap = doc.toObject(FabricScrap.class);
                    if (currentScrap != null) {
                        currentScrap.setId(doc.getId());
                        displayScrap();
                    }
                });
    }

    private void displayScrap() {
        Glide.with(this).load(currentScrap.getImageUrl()).into(ivImage);
        tvTitle.setText(currentScrap.getTitle());
        tvMaterial.setText("Material: " + currentScrap.getMaterialType());
        tvSize.setText("Size: " + currentScrap.getSize());
        tvOwner.setText("Posted by: " + currentScrap.getOwnerName());
        tvLocation.setText("Location: " + currentScrap.getLocation());
        tvDescription.setText(currentScrap.getDescription());

        if (userLat != 0 && currentScrap.getLatitude() != 0) {
            double dist = LocationHelper.calculateDistance(userLat, userLon,
                    currentScrap.getLatitude(), currentScrap.getLongitude());
            tvDistance.setText(LocationHelper.formatDistance(dist));
            tvDistance.setVisibility(View.VISIBLE);
        }

        // Hide request button for own scraps
        String uid = firebaseHelper.getCurrentUserId();
        if (uid != null && uid.equals(currentScrap.getOwnerId())) {
            btnRequest.setVisibility(View.GONE);
        }
    }

    private void showRequestDialog() {
        EditText etMessage = new EditText(this);
        etMessage.setHint("Write a message to the owner...");
        etMessage.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Request Swap")
                .setMessage("Send a swap request for: " + currentScrap.getTitle())
                .setView(etMessage)
                .setPositiveButton("Send Request", (dialog, which) -> {
                    String message = etMessage.getText().toString().trim();
                    sendSwapRequest(message);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendSwapRequest(String message) {
        String uid = firebaseHelper.getCurrentUserId();
        progressBar.setVisibility(View.VISIBLE);
        btnRequest.setEnabled(false);

        firebaseHelper.getFirestore().collection(FirebaseHelper.COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    String requesterName = user != null ? user.getName() : "Unknown";

                    SwapRequest request = new SwapRequest(
                            currentScrap.getId(), currentScrap.getTitle(),
                            currentScrap.getImageUrl(), uid, requesterName,
                            currentScrap.getOwnerId(), message
                    );

                    firebaseHelper.getFirestore()
                            .collection(FirebaseHelper.COLLECTION_REQUESTS)
                            .add(request)
                            .addOnSuccessListener(ref -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show();
                                btnRequest.setText("Request Sent");
                                btnRequest.setEnabled(false);
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnRequest.setEnabled(true);
                                Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
