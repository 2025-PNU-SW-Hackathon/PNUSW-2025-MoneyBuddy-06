package com.moneybuddy.moneylog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceTokenRequest {
    @NotBlank
    private String deviceToken;
}
