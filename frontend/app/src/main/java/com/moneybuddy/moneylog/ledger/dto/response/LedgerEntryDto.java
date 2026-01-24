package com.moneybuddy.moneylog.ledger.dto.response;

import com.google.gson.annotations.SerializedName;

public class LedgerEntryDto {
    @SerializedName("id")         private long id;
    @SerializedName("dateTime")   private String dateTime;   // "YYYY-MM-DDTHH:mm:ss"
    @SerializedName("amount")     private long amount;       // 서버가 부호 적용(+수입/-지출)
    @SerializedName("asset")      private String asset;
    @SerializedName("store")      private String store;
    @SerializedName("category")   private String category;
    @SerializedName("description")private String description;
    @SerializedName("entryType")  private String entryType;  // "INCOME" | "EXPENSE"

    // === getters ===
    public long getId() { return id; }
    public String getDateTime() { return dateTime; }
    public long getAmount() { return amount; }
    public String getAsset() { return asset; }
    public String getStore() { return store; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getEntryType() { return entryType; }

    // (필요하면 setters 도 추가)
    public void setId(long id) { this.id = id; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public void setAmount(long amount) { this.amount = amount; }
    public void setAsset(String asset) { this.asset = asset; }
    public void setStore(String store) { this.store = store; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
}
