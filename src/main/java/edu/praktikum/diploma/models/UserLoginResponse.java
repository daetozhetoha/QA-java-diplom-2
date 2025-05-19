package edu.praktikum.diploma.models;

import lombok.Getter;

@Getter
public class UserLoginResponse {
    private String accessToken;
    private String refreshToken;
}
