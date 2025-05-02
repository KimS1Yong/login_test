package org.example.login_test.model;

public record LoginRequestDto(
        String code,
        String provider // ex: "kakao"
) {}
