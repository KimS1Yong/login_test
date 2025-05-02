package org.example.login_test.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    private Long socialId; // 카카오의 고유 사용자 ID

    @Builder
    public Member(String nickname, Long socialId) {
        this.nickname = nickname;
        this.socialId = socialId;
    }
}
