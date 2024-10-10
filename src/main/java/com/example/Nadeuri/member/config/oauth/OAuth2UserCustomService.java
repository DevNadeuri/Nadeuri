package com.example.Nadeuri.member.config.oauth;

import com.example.Nadeuri.member.MemberEntity;
import com.example.Nadeuri.member.MemberRepository;
import com.example.Nadeuri.member.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException
    {
        OAuth2User user = super.loadUser(userRequest); // ❶ 요청을 바탕으로 유저 정보를 담은 객체 반환
        saveOrUpdate(user);

        return user;
    }

    // ❷ 유저가 있으면 업데이트, 없으면 유저 생성
    private MemberEntity saveOrUpdate(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String userId = (String) attributes.get("sub");
        String profileImageUrl = (String) attributes.get("picture");


        // repository 인스턴스를 통해 메서드 호출
        MemberEntity member = memberRepository.findByEmail(email)
                .map(entity -> {
                    entity.changeEmail(email);
                    entity.changeProfileImage(profileImageUrl); // 기존 사용자 정보 업데이트
                    entity.changeName(name);
                    return entity; // 변경된 엔티티 반환
                })
                .orElse(MemberEntity.builder()
                        .email(email)
                        .name(name)
                        .profileImage(profileImageUrl)
                        .userId(userId)
                        .role(Role.USER)
                        .build());

        return memberRepository.save(member); // repository 인스턴스를 통해 저장
    }

}
