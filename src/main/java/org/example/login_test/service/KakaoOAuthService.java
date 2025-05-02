package org.example.login_test.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.login_test.model.KakaoUserInfo;
import org.example.login_test.model.LoginResponseDto;
import org.example.login_test.model.Member;
import org.example.login_test.model.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final MemberRepository memberRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("토큰 요청 실패", e);
        }
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                request,
                String.class
        );

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            Long id = json.get("id").asLong();
            String nickname = json.path("properties").path("nickname").asText();

            return new KakaoUserInfo(id, nickname);

        } catch (Exception e) {
            throw new RuntimeException("사용자 정보 요청 실패", e);
        }
    }

    public LoginResponseDto kakaoLogin(String code) {
        // 1. 인가 코드로 access token 발급
        String accessToken = getAccessToken(code);

        // 2. access token으로 사용자 정보 조회
        KakaoUserInfo userInfo = getUserInfo(accessToken);

        // 3. DB에서 사용자 조회
        Optional<Member> optionalMember = memberRepository.findMemberBySocialId(userInfo.id());

        Member member = optionalMember.orElseGet(() -> {
            // 사용자 정보가 없으면 새로 등록
            Member newMember = Member.builder()
                    .nickname(userInfo.nickname())
                    .socialId(userInfo.id())
                    .build();
            return memberRepository.save(newMember);
        });

        // 4. JWT 발급 또는 기존 카카오 access token 반환
        return new LoginResponseDto(
                accessToken, // 향후 JWT로 대체 가능
                member.getSocialId(),
                member.getNickname()
        );
    }
}
