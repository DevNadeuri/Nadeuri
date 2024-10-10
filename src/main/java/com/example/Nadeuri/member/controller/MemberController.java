package com.example.Nadeuri.member.controller;

import com.example.Nadeuri.common.response.ApiResponse;
import com.example.Nadeuri.member.*;
import com.example.Nadeuri.member.dto.MemberDTO;
import com.example.Nadeuri.member.dto.request.MemberUpdateRequest;
import com.example.Nadeuri.member.dto.request.SignupDTO;
import com.example.Nadeuri.member.exception.MemberException;
import com.example.Nadeuri.member.security.util.CookieUtil;
import com.example.Nadeuri.member.security.util.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members")
@Log4j2
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    //마무리되면 DTO들에 @Validated 어노테이션 추가 필요

    @PostMapping("/sign-up") //회원가입
    public ResponseEntity<ApiResponse> signUp(@RequestPart("signUpDTO") SignupDTO signUpDTO, @RequestPart(value = "image", required = false) final MultipartFile profileImage) {
         memberService.signUp(signUpDTO, profileImage);

        return ResponseEntity.ok(ApiResponse.success(null));
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@RequestBody MemberDTO memberDTO, HttpServletResponse response) {
        MemberEntity member = memberRepository.findByUserId(memberDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 Id 입니다."));
        if (!passwordEncoder.matches(memberDTO.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        //사용자 정보 가져오기
        MemberDTO foundMemberDTO = memberService.read(memberDTO.getUserId(), memberDTO.getPassword());

        //토큰 생성
        Map<String, Object> payloadMap = foundMemberDTO.getPayload();
        String accessToken = jwtUtil.createToken(payloadMap, 120);    //60분 유효
        String refreshToken = jwtUtil.createToken(Map.of("userId", foundMemberDTO.getUserId()),
                60 * 24 * 7);                   //7일 유효

        // 쿠키에 토큰 저장
        CookieUtil.addCookie(response, "accessToken", accessToken, 3600); // 1시간
        CookieUtil.addCookie(response, "refreshToken", refreshToken, 604800); // 7일

        log.info("--- accessToken : " + accessToken);
        log.info("--- refreshToken : " + refreshToken);


        return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> update(@PathVariable("userId") String userId,
                                              @RequestPart("memberUpdateRequest") MemberUpdateRequest memberUpdateRequest,
                                              @RequestPart(value = "image", required = false) final MultipartFile profileImage,
                                              Authentication authentication
    ) {
        String dbUserId = authentication.getName();
        log.info(dbUserId);

        if(!authentication.getName().equals(userId)||  //인증 사용자와 DTO의 사용자가 불일치
                !dbUserId.equals(userId)){  //
            throw MemberException.NOT_MATCHED_USER.get();
        }

        memberService.updateMember(userId, memberUpdateRequest, profileImage);
        log.info(memberUpdateRequest.getRole());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> delete(@PathVariable("userId") String userId, Authentication authentication) {
        String dbUserId = authentication.getName();
        log.info(dbUserId);

        if(!authentication.getName().equals(userId)||  //인증 사용자와 DTO의 사용자가 불일치
                !dbUserId.equals(userId)){  //
            throw MemberException.NOT_MATCHED_USER.get();
        }

        memberService.deleteMember(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberDTO>> getCurrentUser(Authentication authentication) {
        String userId = authentication.getName(); // 인증된 사용자 ID 가져오기
        MemberDTO memberDTO = new MemberDTO(memberService.findByUserId(userId)); // 사용자 정보 조회
        return ResponseEntity.ok(ApiResponse.success(memberDTO)); // 사용자 정보를 포함한 ApiResponse 반환
    }
}
