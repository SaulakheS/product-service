package com.zestindia.productservice.event;

public class ProductActivityEvent {

    public enum ActivityType {
        CREATED,
        UPDATED,
        DELETED,
        ITEM_ADDED,
        ITEM_UPDATED,
        ITEM_DELETED
    }

    private final ActivityType activityType;
    private final Integer productId;
    private final String productName;
    private final String triggeredBy;
    private final long timestamp;

    public ProductActivityEvent(ActivityType activityType, Integer productId, String productName, String triggeredBy) {
        this.activityType = activityType;
        this.productId = productId;
        this.productName = productName;
        this.triggeredBy = triggeredBy;
        this.timestamp = System.currentTimeMillis();
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ProductActivityEvent{" +
                "activityType=" + activityType +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", triggeredBy='" + triggeredBy + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
