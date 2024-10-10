package com.example.Nadeuri.member.security.filter;


import com.example.Nadeuri.member.Role;
import com.example.Nadeuri.member.security.auth.CustomUserPrincipal;
import com.example.Nadeuri.member.security.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    @Override             //필터링 적용 X
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        log.info("--- shouldNotFilter()");
        log.info("--- requestURI : " + request.getRequestURI());

        if (request.getRequestURI().startsWith("/v1/token/")) {
            //토큰 발급 관련 경로는 제외
            return true;
        }
        if (request.getRequestURI().startsWith("/v1/members/sign-up")) {
            //토큰 발급 관련 경로는 제외
            return true;
        }
        if (request.getRequestURI().startsWith("/v1/members/login")) {
            //토큰 발급 관련 경로는 제외
            return true;
        }

        if (request.getRequestURI().startsWith("/members/sign-up")) {
            //토큰 발급 관련 경로는 제외
            return true;
        }

        if (request.getRequestURI().startsWith("/members/login")) {
            //토큰 발급 관련 경로는 제외
            return true;
        }

        if (request.getRequestURI().startsWith("/boards/list")) {
            //토큰 발급 관련 경로는 제외
            return true;
        }

        if (request.getRequestURI().startsWith("/img/")) {
            //토큰 발급 관련 경로는 제외
            return true;
        }

        if (request.getRequestURI().startsWith("/oauthlogin")) {
            //토큰 발급 관련 경로는 제외
            return true;
        }

        if (request.getRequestURI().startsWith("/success")) {
            //토큰 발급 관련 경로는 제외
            return true;
        }

        // /v1/boards는 토큰이 없어도 접근 가능
        if ("/v1/boards".equals(request.getRequestURI())) {
            return true;
        }

        // /v1/boards는 토큰이 없어도 접근 가능
        if ("/boards/".equals(request.getRequestURI())) {
            return true;
        }


//        if(!request.getRequestURI().startsWith("/v1/")) {   //application.properties에서 static설정도 추가해줘야함
//            return true;
//        }

        return false;    //그 외 모든 경로는 필터링 false // true 모든 경로 허용
    }

    @Override             //필터링 적용 O - 액세스 토큰 확인
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("--- doFilterInternal() ");
        log.info("--- requestURI : " + request.getRequestURI());

        // Authorization 헤더에서 액세스 토큰 읽기
        String headerAuth = request.getHeader("Authorization");
        log.info("--- headerAuth : " + headerAuth);

        //액세스 토큰이 없거나 'Bearer ' 가 아니면 403 예외 발생
        if (headerAuth == null || !headerAuth.startsWith("Bearer ")) {   //Bearer 옆에 공백 필수 !
            handleException(response, new Exception("ACCESS TOKEN NOT FOUND"));
            return;
        }

//        -----쿠키에 토큰값을 저장하였으므로 쿠키에서 토큰값을 가져와야한다.-----
//        String accessToken = null;
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if (cookie.getName().equals("accessToken")) {
//                    accessToken = cookie.getValue();
//                    break;
//                }
//            }
//        }
//
//        log.info("--- accessToken : " + accessToken);
//
//        // 액세스 토큰이 없으면 403 예외 발생
//        if (accessToken == null) {
//            handleException(response, new Exception("ACCESS TOKEN NOT FOUND"));
//            return;
//        }

        //토큰 유효성 검증 -----------------------------------------------
        String accessToken = headerAuth.substring(7);  //"Bearer " 를 제외하고 토큰값 저장
        try {
            Map<String, Object> claims = jwtUtil.validateToken(accessToken);
            log.info("--- 토큰 유효성 검증 완료 ---");

            //SecurityContext 처리 ------------------------------------------
            String userId = claims.get("userId").toString();
            String roleString = claims.get("role").toString();
            Role role = Role.valueOf(roleString);

            Set<Role> authorities = role.getAuthorities();

            authorities.forEach(authority -> log.info("beforeAuthority: " + authority.name()));

            // Spring Security의 권한 객체로 변환
            List<GrantedAuthority> grantedAuthorities = authorities.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                    .collect(Collectors.toList());

            grantedAuthorities.forEach(authority -> logger.info("Granted Authority: {}"+ authority.getAuthority()));



            //토큰을 이용하여 인증된 정보 저장
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    new CustomUserPrincipal(userId), null, grantedAuthorities
            );

            //SecurityContext에 인증/인가 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response); //검증 결과 문제가 없는 경우
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    public void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().println("{\"error\": \"" + e.getMessage() + "\"}");
    }
}
