package com.kutira.kone.models;

import com.google.firebase.Timestamp;

public class SwapRequest {
    private String id;
    private String scrapId;
    private String scrapTitle;
    private String scrapImageUrl;
    private String requesterId;
    private String requesterName;
    private String ownerId;
    private String message;
    private String status; // pending, accepted, rejected
    private Timestamp createdAt;

    public SwapRequest() {}

    public SwapRequest(String scrapId, String scrapTitle, String scrapImageUrl,
                       String requesterId, String requesterName, String ownerId, String message) {
        this.scrapId = scrapId;
        this.scrapTitle = scrapTitle;
        this.scrapImageUrl = scrapImageUrl;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.ownerId = ownerId;
        this.message = message;
        this.status = "pending";
        this.createdAt = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getScrapId() { return scrapId; }
    public void setScrapId(String scrapId) { this.scrapId = scrapId; }
    public String getScrapTitle() { return scrapTitle; }
    public void setScrapTitle(String scrapTitle) { this.scrapTitle = scrapTitle; }
    public String getScrapImageUrl() { return scrapImageUrl; }
    public void setScrapImageUrl(String scrapImageUrl) { this.scrapImageUrl = scrapImageUrl; }
    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
