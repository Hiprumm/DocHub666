package com.example.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * 图形验证码响应出参 DTO。
 */
@Getter
@AllArgsConstructor
public class CaptchaResp implements Serializable {

    /** 验证码记录 ID（提交登录时回传） */
    private String captchaId;

    /** 验证码图片（base64 data URL） */
    private String imageBase64;
}