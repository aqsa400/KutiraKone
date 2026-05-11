package com.kutira.kone.models;

import com.google.firebase.Timestamp;

public class FabricScrap {
    private String id;
    private String title;
    private String description;
    private String materialType;
    private String size;
    private String imageUrl;
    private String ownerId;
    private String ownerName;
    private String ownerPhone;
    private double latitude;
    private double longitude;
    private String location;
    private boolean available;
    private Timestamp createdAt;

    public FabricScrap() {}

    public FabricScrap(String title, String description, String materialType, String size,
                       String imageUrl, String ownerId, String ownerName, String ownerPhone,
                       double latitude, double longitude, String location) {
        this.title = title;
        this.description = description;
        this.materialType = materialType;
        this.size = size;
        this.imageUrl = imageUrl;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.available = true;
        this.createdAt = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
