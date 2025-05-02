package org.example.login_test.controller;

import lombok.RequiredArgsConstructor;
import org.example.login_test.model.KakaoUserInfo;
import org.example.login_test.model.LoginRequestDto;
import org.example.login_test.model.LoginResponseDto;
import org.example.login_test.service.KakaoOAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LoginController {

    private final KakaoOAuthService kakaoOAuthService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> socialLogin(@RequestBody LoginRequestDto request) {
        if (!"kakao".equalsIgnoreCase(request.provider())) {
            return ResponseEntity.badRequest().build();
        }

        // 1. 인가 코드로 액세스 토큰 요청
        // 2. 액세스 토큰으로 사용자 정보 조회
        // 3. 사용자 등록/로그인 로직 처리 (여기선 생략)
        LoginResponseDto response = kakaoOAuthService.kakaoLogin(request.code());

        // 4. 응답 DTO 반환
        return ResponseEntity.ok(new LoginResponseDto(
                response.accessToken(), // 실제 서비스에서는 JWT로 교체할 수 있음
                response.socialId(),
                response.nickname()
        ));
    }
}
