package com.mainproject.back.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainproject.back.member.entity.Member;
import com.mainproject.back.security.dto.LoginDto;
import com.mainproject.back.security.jwt.JwtTokenizer;
import java.io.IOException;
import javax.servlet.ServletException;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenizer jwtTokenizer;

  public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
      JwtTokenizer jwtTokenizer) {
    this.authenticationManager = authenticationManager;
    this.jwtTokenizer = jwtTokenizer;
  }

  @SneakyThrows
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) {

    ObjectMapper objectMapper = new ObjectMapper();
    LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

    return authenticationManager.authenticate(authenticationToken);
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult) throws ServletException, IOException {
    Member member = (Member) authResult.getPrincipal();

    String accessToken = delegateAccessToken(member);
    String refreshToken = delegateRefreshToken(member);

    response.setHeader("Authorization", "Bearer " + accessToken);
    response.setHeader("Refresh", refreshToken);
    response.addHeader("Access-Control-Expose-Headers", "Authorization, Refresh");

    this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
  }

  private String delegateAccessToken(Member member) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("username", member.getEmail());
    claims.put("roles", member.getRoles());

    String subject = member.getEmail();
    Date expiration = jwtTokenizer.getTokenExpiration(
        jwtTokenizer.getAccessTokenExpirationMinutes());

    String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

    String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration,
        base64EncodedSecretKey);

    return accessToken;
  }

  private String delegateRefreshToken(Member member) {
    String subject = member.getEmail();
    Date expiration = jwtTokenizer.getTokenExpiration(
        jwtTokenizer.getRefreshTokenExpirationMinutes());
    String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

    String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration,
        base64EncodedSecretKey);

    return refreshToken;
  }
}