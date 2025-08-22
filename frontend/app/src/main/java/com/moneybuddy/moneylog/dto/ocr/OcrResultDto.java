package com.moneybuddy.moneylog.dto.ocr;

public class OcrResultDto {
    public long id;
    public String dateTime;
    public long amount;        // 응답은 부호 포함
    public String asset;
    public String store;
    public String category;
    public String description;
    public String entryType;   // "EXPENSE"
}
