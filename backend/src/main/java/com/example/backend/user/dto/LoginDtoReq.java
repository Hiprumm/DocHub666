package com.example.backend.user.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginDtoReq {
    private String name;
    private String password;
}
