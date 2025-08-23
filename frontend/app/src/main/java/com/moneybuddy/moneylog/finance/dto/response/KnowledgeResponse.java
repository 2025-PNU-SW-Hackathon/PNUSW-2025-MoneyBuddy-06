package com.moneybuddy.moneylog.finance.dto.response;

import com.google.gson.annotations.SerializedName;

public class KnowledgeResponse {

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}