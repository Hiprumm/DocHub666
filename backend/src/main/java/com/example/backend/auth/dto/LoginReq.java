package com.example.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录请求入参 DTO（含 Jakarta Validation 中文提示）。
 */
@Getter
@Setter
public class LoginReq {

    /** 登录账号 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 登录密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 图形验证码 ID（启用验证码时必填，未启用可空） */
    private String captchaId;

    /** 图形验证码内容（启用验证码时必填，未启用可空） */
    private String captchaCode;
}