package com.example.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 刷新令牌请求入参 DTO（含 Jakarta Validation 中文提示）。
 */
@Getter
@Setter
public class RefreshReq {

    /** 长效刷新令牌（Refresh Token） */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}