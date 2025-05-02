package org.example.login_test.model;

public record LoginResponseDto(
        String accessToken, // 추후 JWT 사용 시 토큰 넣기
        Long socialId,
        String nickname
) {}