package com.kutira.kone.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kutira.kone.R;
import com.kutira.kone.adapters.ScrapAdapter;
import com.kutira.kone.models.FabricScrap;
import com.kutira.kone.utils.FirebaseHelper;
import com.kutira.kone.utils.LocationHelper;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ScrapAdapter adapter;
    private List<FabricScrap> scrapList, filteredList;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private Spinner spinnerFilter, spinnerRadius;
    private FirebaseHelper firebaseHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLat = 0, userLon = 0;
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseHelper = FirebaseHelper.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        spinnerRadius = findViewById(R.id.spinnerRadius);
        FloatingActionButton fabUpload = findViewById(R.id.fabUpload);

        scrapList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ScrapAdapter(filteredList, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        setupSpinners();
        getLocation();
        loadScraps();

        fabUpload.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UploadScrapActivity.class)));

        swipeRefresh.setOnRefreshListener(this::loadScraps);
    }

    private void setupSpinners() {
        String[] materials = {"All", "Silk", "Cotton", "Wool", "Polyester", "Denim", "Other"};
        ArrayAdapter<String> materialAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, materials);
        materialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(materialAdapter);

        String[] radii = {"All distances", "1 km", "2 km", "5 km", "10 km"};
        ArrayAdapter<String> radiusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, radii);
        radiusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRadius.setAdapter(radiusAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerFilter.setOnItemSelectedListener(filterListener);
        spinnerRadius.setOnItemSelectedListener(filterListener);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLon = location.getLongitude();
                applyFilters();
            }
        });
    }

    private void loadScraps() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getFirestore()
                .collection(FirebaseHelper.COLLECTION_SCRAPS)
                .whereEqualTo("available", true)
                .addSnapshotListener((snapshots, error) -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    if (error != null || snapshots == null) return;

                    scrapList.clear();
                    for (var doc : snapshots.getDocuments()) {
                        FabricScrap scrap = doc.toObject(FabricScrap.class);
                        if (scrap != null) {
                            scrap.setId(doc.getId());
                            scrapList.add(scrap);
                        }
                    }
                    applyFilters();
                });
    }

    private void applyFilters() {
        String selectedMaterial = spinnerFilter.getSelectedItem() != null ?
                spinnerFilter.getSelectedItem().toString() : "All";
        String selectedRadius = spinnerRadius.getSelectedItem() != null ?
                spinnerRadius.getSelectedItem().toString() : "All distances";

        double maxDistKm = Double.MAX_VALUE;
        switch (selectedRadius) {
            case "1 km": maxDistKm = 1; break;
            case "2 km": maxDistKm = 2; break;
            case "5 km": maxDistKm = 5; break;
            case "10 km": maxDistKm = 10; break;
        }

        filteredList.clear();
        for (FabricScrap scrap : scrapList) {
            boolean materialMatch = selectedMaterial.equals("All") ||
                    scrap.getMaterialType().equalsIgnoreCase(selectedMaterial);

            boolean radiusMatch = true;
            if (maxDistKm != Double.MAX_VALUE && userLat != 0) {
                double dist = LocationHelper.calculateDistance(userLat, userLon,
                        scrap.getLatitude(), scrap.getLongitude());
                radiusMatch = dist <= maxDistKm;
            }

            if (materialMatch && radiusMatch) {
                filteredList.add(scrap);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_requests) {
            startActivity(new Intent(this, MyRequestsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            firebaseHelper.getAuth().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFilters();
    }
}
