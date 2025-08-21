package com.moneybuddy.moneylog.data.dto.ledger;

import com.google.gson.annotations.SerializedName;

public class LedgerEntryDto {
    @SerializedName("id")        public long id;
    @SerializedName("dateTime")  public String dateTime;   // "2025-08-01T08:41:00"
    @SerializedName("amount")    public long amount;       // 음수/양수 그대로
    @SerializedName("asset")     public String asset;
    @SerializedName("store")     public String store;
    @SerializedName("category")  public String category;
    @SerializedName("description") public String description;
    @SerializedName("entryType") public String entryType;  // "INCOME" | "EXPENSE"
}
