package com.example.Nadeuri.member.controller;

import com.example.Nadeuri.member.MemberService;
import com.example.Nadeuri.member.dto.MemberDTO;
import com.example.Nadeuri.member.dto.request.MemberUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class   MemberViewController {
    private final MemberService memberService;

    @GetMapping("/login")
    public String login() {
        return "login"; // login.html 파일을 반환
    }


    @GetMapping("/sign-up")
    public String signUp() {
        return "sign-up"; // sign-up.html 파일을 반환
    }

    //8 로그아웃 메서드 추가
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/members/login";
    }

    // 회원정보 수정 페이지를 반환하는 메서드
    @GetMapping("/edit")
    public String showEditProfile(Model model) {
        // SecurityContextHolder에서 인증 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();


        MemberDTO memberDTO = new MemberDTO(memberService.findByUserId(userId));
        model.addAttribute("member", memberDTO);
        return "member/edit";  // 회원정보 수정 페이지로 이동 (Thymeleaf 템플릿 등)
    }

    // 회원정보 수정 처리
    @PostMapping("/{userId}")
    public String updateMember(@PathVariable("userId") String userId,
                               @ModelAttribute MemberUpdateRequest memberUpdateRequest,
                               @RequestPart(value = "image", required = false) MultipartFile profileImage,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        String dbUserId = authentication.getName();
        if (!dbUserId.equals(userId)) {
            throw new IllegalArgumentException("인증 사용자와 수정 대상이 일치하지 않습니다.");
        }

        memberService.updateMember(userId, memberUpdateRequest, profileImage);

        redirectAttributes.addFlashAttribute("message", "회원정보가 성공적으로 수정되었습니다.");
        return "redirect:/boards/list";
    }

}
