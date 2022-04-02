package me.whizvox.otdl.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OTDLLoginFailureHandler implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
    String errorType;
    if (exception instanceof BadCredentialsException) {
      errorType = "invalid";
    } else if (exception instanceof DisabledException) {
      errorType = "unverified";
    } else {
      errorType = "error";
    }
    response.sendRedirect("/login?" + errorType);
  }

}
