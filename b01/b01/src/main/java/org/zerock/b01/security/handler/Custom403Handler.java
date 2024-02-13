package org.zerock.b01.security.handler;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
public class Custom403Handler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.info("------------Access Denied---------------");

        // 403 응답상태 코드 설정
        resp.setStatus(HttpStatus.FORBIDDEN.value());

        // Json 요청 여부 확인
        String contentType = req.getHeader("Content-Type");
        boolean jsonRequest = contentType.startsWith("application/json");

        log.info("isJSON: " + jsonRequest);

        // 일반 요청일 때 Access Denied
        if(!jsonRequest) {
            resp.sendRedirect("/member/login?error=ACCESS_DENIED");
        }

    }

}
