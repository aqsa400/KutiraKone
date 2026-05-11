package com.kutira.kone.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.storage.StorageReference;
import com.kutira.kone.R;
import com.kutira.kone.models.FabricScrap;
import com.kutira.kone.models.User;
import com.kutira.kone.utils.FirebaseHelper;
import java.util.UUID;

public class UploadScrapActivity extends AppCompatActivity {

    private ImageView ivScrapImage;
    private EditText etTitle, etDescription, etSize, etLocation;
    private Spinner spinnerMaterial;
    private Button btnSelectImage, btnUpload;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private FirebaseHelper firebaseHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLat = 0, userLon = 0;
    private static final int LOCATION_REQUEST = 101;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivScrapImage.setImageURI(uri);
                    ivScrapImage.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_scrap);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseHelper = FirebaseHelper.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ivScrapImage = findViewById(R.id.ivScrapImage);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etSize = findViewById(R.id.etSize);
        etLocation = findViewById(R.id.etLocation);
        spinnerMaterial = findViewById(R.id.spinnerMaterial);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);
        progressBar = findViewById(R.id.progressBar);

        String[] materials = {"Silk", "Cotton", "Wool", "Polyester", "Denim", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, materials);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaterial.setAdapter(adapter);

        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnUpload.setOnClickListener(v -> validateAndUpload());

        getLocation();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLon = location.getLongitude();
            }
        });
    }

    private void validateAndUpload() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String size = etSize.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String material = spinnerMaterial.getSelectedItem().toString();

        if (TextUtils.isEmpty(title)) { etTitle.setError("Enter title"); return; }
        if (TextUtils.isEmpty(size)) { etSize.setError("Enter size"); return; }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);

        String uid = firebaseHelper.getCurrentUserId();
        String fileName = "scraps/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = firebaseHelper.getStorage().getReference().child(fileName);

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            fetchUserAndSave(title, description, material, size,
                                    uri.toString(), uid, location);
                        }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpload.setEnabled(true);
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void fetchUserAndSave(String title, String description, String material,
                                   String size, String imageUrl, String uid, String locationText) {
        firebaseHelper.getFirestore().collection(FirebaseHelper.COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    String ownerName = user != null ? user.getName() : "Unknown";
                    String ownerPhone = user != null ? user.getPhone() : "";

                    FabricScrap scrap = new FabricScrap(title, description, material, size,
                            imageUrl, uid, ownerName, ownerPhone, userLat, userLon, locationText);

                    firebaseHelper.getFirestore()
                            .collection(FirebaseHelper.COLLECTION_SCRAPS)
                            .add(scrap)
                            .addOnSuccessListener(ref -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Scrap uploaded successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnUpload.setEnabled(true);
                                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
