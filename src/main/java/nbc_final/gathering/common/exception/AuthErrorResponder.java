package nbc_final.gathering.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthErrorResponder implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        handleUnauthorizedResponse(response, authException);
    }

    private void handleUnauthorizedResponse(HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(buildUnauthorizedResponse(authException.getMessage()));
    }

    private String buildUnauthorizedResponse(String message) {
        return "{\"statusCode\": 401, \"message\": \"" + message + "\", \"data\": null}";
    }
}

