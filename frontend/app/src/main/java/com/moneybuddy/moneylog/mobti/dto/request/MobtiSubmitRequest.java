package com.moneybuddy.moneylog.mobti.dto.request;

import java.util.List;

public class MobtiSubmitRequest {
    private List<String> answers;

    public MobtiSubmitRequest(List<String> answers) {
        this.answers = answers;
    }
    public List<String> getAnswers() { return answers; }
    public void setAnswers(List<String> answers) { this.answers = answers; }
}
