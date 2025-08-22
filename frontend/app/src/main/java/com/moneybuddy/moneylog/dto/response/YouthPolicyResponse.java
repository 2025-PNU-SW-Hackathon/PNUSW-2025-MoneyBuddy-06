package com.moneybuddy.moneylog.dto.response;

import com.google.gson.annotations.SerializedName;
import android.os.Parcel;
import android.os.Parcelable;

public class YouthPolicyResponse implements Parcelable {

    @SerializedName("id")
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("applicationPeriod")
    private String applicationPeriod;

    @SerializedName("amount")
    private String amount;

    @SerializedName("benefit")
    private String benefit;

    @SerializedName("eligibility")
    private String eligibility;

    @SerializedName("applicationMethod")
    private String applicationMethod;

    @SerializedName("description")
    private String description;

    @SerializedName("url")
    private String url;


    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getApplicationPeriod() { return applicationPeriod; }
    public String getAmount() { return amount; }
    public String getBenefit() { return benefit; }
    public String getEligibility() { return eligibility; }
    public String getApplicationMethod() { return applicationMethod; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }


    protected YouthPolicyResponse(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        title = in.readString();
        applicationPeriod = in.readString();
        amount = in.readString();
        benefit = in.readString();
        eligibility = in.readString();
        applicationMethod = in.readString();
        description = in.readString();
        url = in.readString();
    }

    public static final Creator<YouthPolicyResponse> CREATOR = new Creator<YouthPolicyResponse>() {
        @Override
        public YouthPolicyResponse createFromParcel(Parcel in) {
            return new YouthPolicyResponse(in);
        }

        @Override
        public YouthPolicyResponse[] newArray(int size) {
            return new YouthPolicyResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(title);
        dest.writeString(applicationPeriod);
        dest.writeString(amount);
        dest.writeString(benefit);
        dest.writeString(eligibility);
        dest.writeString(applicationMethod);
        dest.writeString(description);
        dest.writeString(url);
    }
}