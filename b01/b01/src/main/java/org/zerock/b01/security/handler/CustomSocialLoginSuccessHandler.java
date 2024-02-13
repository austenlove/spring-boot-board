package org.zerock.b01.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.zerock.b01.dto.MemberSecurityDTO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
@RequiredArgsConstructor
public class CustomSocialLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication authentication) throws IOException, ServletException {

        log.info("--------------------------------------------------");
        log.info("CustomLoginSuccessHandler onAuthenticationSuccess....................");
        log.info(authentication.getPrincipal());

        MemberSecurityDTO memberSecurityDTO = (MemberSecurityDTO) authentication.getPrincipal();

        String encodedPw = memberSecurityDTO.getMpw();

        // 소셜 로그인이고 회원 패스워드가 기본 1111 -> 비밀번호 변경 요청 페이지
        if(memberSecurityDTO.isSocial() && (memberSecurityDTO.getMpw().equals("1111") || passwordEncoder.matches("1111", memberSecurityDTO.getMpw()))) {

            log.info("Should Change Password");
            log.info("Redirect to Member Modify");
            resp.sendRedirect("/member/modify");
            return;

        } else {

            resp.sendRedirect("/board/list");
        }

    }


}
