package com.bootcamp.ecommerce_rohit.security;

import com.bootcamp.ecommerce_rohit.entities.AccessToken;
import com.bootcamp.ecommerce_rohit.entities.RefreshToken;
import com.bootcamp.ecommerce_rohit.exceptionsHandling.InvalidParametersException;
import com.bootcamp.ecommerce_rohit.repositories.AccessTokenRepository;
import com.bootcamp.ecommerce_rohit.repositories.RefreshTokenRepository;
import com.bootcamp.ecommerce_rohit.repositories.UserRepository;
import com.bootcamp.ecommerce_rohit.services.JwtUtilsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtilsService jwtUtilsService;

    @Autowired
    private NewUserDetailService newUserDetailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    private static final List<String> EXCLUDED_URLS = List.of(
            "/customer/signup", "/login/admin", "/reset-password","/seller/signup","request-resetpassword",
            "/customer/register-verification", "/login/customer", "/login/seller","/customer/resend-verification"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return EXCLUDED_URLS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = null;

        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("accessToken")) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token != null && accessTokenRepository.findByToken(token) == null) {
                throw new InvalidParametersException("Invalid access token");
            }

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtUtilsService.extractUsername(token);
                UserDetails userDetails = newUserDetailService.loadUserByUsername(username);

                if (jwtUtilsService.validateToken(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            handleRefreshToken(e, request, response, filterChain);
        } catch (InvalidParametersException ex) {
            handleException(response, ex.getMessage(), HttpServletResponse.SC_UNAUTHORIZED, request.getRequestURI());
        }
    }

    private void handleRefreshToken(ExpiredJwtException e, HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String email = e.getClaims().getSubject();

        if (email != null) {
            RefreshToken refreshToken = refreshTokenRepository.findByEmail(email);
            if (refreshToken != null && jwtUtilsService.validateToken(refreshToken.getRefreshToken(), email)) {

                String newAccessToken = jwtUtilsService.generateToken(email, 15);
                accessTokenRepository.deleteByEmail(email);
                accessTokenRepository.save(new AccessToken(newAccessToken, email));

                String username = jwtUtilsService.extractUsername(newAccessToken);
                UserDetails userDetails = newUserDetailService.loadUserByUsername(username);

                if (jwtUtilsService.validateToken(newAccessToken, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

                filterChain.doFilter(request, response);
            } else {
                handleException(response, "Refresh token expired or invalid", HttpServletResponse.SC_UNAUTHORIZED, request.getRequestURI());
            }
        }
    }

    private void handleException(HttpServletResponse response, String message, int status, String path) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status,
                "error", "Unauthorized",
                "message", message,
                "path", path
        ));
    }
}
