package com.bootcamp.ecommerce_rohit.services;

import com.bootcamp.ecommerce_rohit.entities.AccessToken;
import com.bootcamp.ecommerce_rohit.entities.RefreshToken;
import com.bootcamp.ecommerce_rohit.entities.Role;
import com.bootcamp.ecommerce_rohit.entities.User;
import com.bootcamp.ecommerce_rohit.repositories.AccessTokenRepository;
import com.bootcamp.ecommerce_rohit.repositories.RefreshTokenRepository;
import com.bootcamp.ecommerce_rohit.repositories.RoleRepository;
import com.bootcamp.ecommerce_rohit.repositories.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

@Service
public class AuthService {
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtUtilsService jwtUtilsService;
    @Autowired
    EmailService emailService;
    @Autowired
    AccessTokenRepository accessTokenRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    public ResponseEntity<String> userLogin(String username, String password, HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            try {


                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        if (jwtUtilsService.isTokenExpired(token) || !jwtUtilsService.isTokenExpired(token)) {
                            return new ResponseEntity<>("Already logged in. Please logout first.", HttpStatus.BAD_REQUEST);
                        }
                    }
                }
            } catch (ExpiredJwtException e) {
                return new ResponseEntity<>("Remove token from cookie first", HttpStatus.BAD_REQUEST);
            }
        }
        User user = userRepository.findByEmail(username);
        if (user == null) {
            return new ResponseEntity<>("user not Found!!", HttpStatus.NOT_FOUND);
        }
        if (!user.getIsActive()) {
            return new ResponseEntity<>("This user is not activated", HttpStatus.FORBIDDEN);
        }
        if (user.getIsLocked()) {
            return new ResponseEntity<>("This user is locked! Contact Admin", HttpStatus.FORBIDDEN);
        }
        LocalDateTime lastUpdated = user.getPasswordUpdateDate();
        LocalDateTime expiryDate = lastUpdated.plusDays(20);

        if (expiryDate.isBefore(LocalDateTime.now())) {
            return new ResponseEntity<>("Password expired!! update it using forget password", HttpStatus.BAD_REQUEST);
        }
        ;

        if (passwordEncoder.matches(password, user.getPassword())) {
            if (accessTokenRepository.findByEmail(user.getEmail()).isPresent()) {
                accessTokenRepository.delete(accessTokenRepository.findByEmail(user.getEmail()).get());
            }
            String token = jwtUtilsService.generateToken(user.getEmail(), 24*60);
            AccessToken accessToken = new AccessToken();
            accessToken.setToken(token);
            accessToken.setEmail(user.getEmail());
            accessTokenRepository.save(accessToken);

            //delete refresh token from db if present
            if (refreshTokenRepository.findByEmail(user.getEmail()) != null) {
                refreshTokenRepository.deleteByEmail(user.getEmail());
            }

            //generate refresh token
            String refreshToken = jwtUtilsService.generateToken(user.getEmail(), 60 * 24);
            RefreshToken refreshTokenEntity = new RefreshToken(refreshToken, Instant.now(), user.getEmail());
            refreshTokenRepository.save(refreshTokenEntity);

            Cookie cookie = new Cookie("accessToken", token);
            cookie.setPath("/");
            cookie.setSecure(true);
            cookie.setHttpOnly(true);

            response.addCookie(cookie);
            user.setFailLoginAttemptsCount(0);
            userRepository.save(user);
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            if (user.getFailLoginAttemptsCount() == null) {
                user.setFailLoginAttemptsCount(0);
            }
            user.setFailLoginAttemptsCount(user.getFailLoginAttemptsCount() + 1);
            userRepository.save(user);
            if (user.getFailLoginAttemptsCount() >= 3) {
                user.setIsLocked(true);
                userRepository.save(user);
                try {
                    emailService.sendEmail(user.getEmail(), "Account Locked",
                            "You Entered 3 wrong Passwords,Account Locked");
                } catch (
                        MessagingException e) {
                    return new ResponseEntity<>("Error sending Verification email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return new ResponseEntity<>("3 continuous login attempts with invalid password!! account locked", HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("Invalid password", HttpStatus.BAD_REQUEST);
        }
    }

        public ResponseEntity<String> customerLogout (HttpServletRequest request, HttpServletResponse response){
            Cookie[] cookies = request.getCookies();
            String token = null;
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("accessToken")) {
                        token = cookie.getValue();
                    }
                }
            }
            String customerEmail;
            try {
                customerEmail = jwtUtilsService.extractUsername(token);
            } catch (ExpiredJwtException e) {
                return new ResponseEntity<>("Token expired!,logout failed", HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                return new ResponseEntity<>("invalid token logout Failed!!", HttpStatus.BAD_REQUEST);
            }
            User validUser = userRepository.findByEmail(customerEmail);
            AccessToken dbtoken = accessTokenRepository.findByEmail(customerEmail).orElseThrow();
            if (!token.equals(dbtoken.getToken())) {
                return new ResponseEntity<>("incorrect token logout Failed!!", HttpStatus.BAD_REQUEST);
            }

            validUser.setFailLoginAttemptsCount(0);
            accessTokenRepository.delete(dbtoken);
            userRepository.save(validUser);

            Cookie cookie = new Cookie("accessToken", null);
            cookie.setPath("/");
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);

            response.addCookie(cookie);

            refreshTokenRepository.deleteByEmail(customerEmail);
            return new ResponseEntity<>("Customer logged out Successfully! ", HttpStatus.OK);
        }

        public ResponseEntity<String> sellerLogout (HttpServletRequest request, HttpServletResponse response){
            Cookie[] cookies = request.getCookies();
            String token = null;
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("accessToken")) {
                        token = cookie.getValue();
                    }
                }
            }
            String sellerEmail;
            try {
                sellerEmail = jwtUtilsService.extractUsername(token);
            } catch (ExpiredJwtException e) {
                return new ResponseEntity<>("Token expired!,logout failed", HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                return new ResponseEntity<>("invalid token logout Failed!!", HttpStatus.BAD_REQUEST);
            }
            User validUser = userRepository.findByEmail(sellerEmail);
            AccessToken dbtoken = accessTokenRepository.findByEmail(sellerEmail).orElseThrow();
            if (!token.equals(dbtoken.getToken())) {
                return new ResponseEntity<>("incorrect token logout Failed!!", HttpStatus.BAD_REQUEST);
            }

            validUser.setFailLoginAttemptsCount(0);
            accessTokenRepository.delete(dbtoken);
            userRepository.save(validUser);

            Cookie cookie = new Cookie("accessToken", null);
            cookie.setPath("/");
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);

            response.addCookie(cookie);

            refreshTokenRepository.deleteByEmail(sellerEmail);
            return new ResponseEntity<>("seller logged out Successfully! ", HttpStatus.OK);
        }

        public ResponseEntity<String> adminLogout (HttpServletRequest request, HttpServletResponse response){
            Cookie[] cookies = request.getCookies();
            String token = null;
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("accessToken")) {
                        token = cookie.getValue();
                    }
                }
            }
            String adminEmail;
            try {
                adminEmail = jwtUtilsService.extractUsername(token);
            } catch (ExpiredJwtException e) {
                return new ResponseEntity<>("Token expired!,logout failed", HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                return new ResponseEntity<>("invalid token logout Failed!!", HttpStatus.BAD_REQUEST);
            }
            User validUser = userRepository.findByEmail(adminEmail);
            AccessToken dbtoken = accessTokenRepository.findByEmail(adminEmail).orElseThrow();
            if (!token.equals(dbtoken.getToken())) {
                return new ResponseEntity<>("incorrect token logout Failed!!", HttpStatus.BAD_REQUEST);
            }

            validUser.setFailLoginAttemptsCount(0);
            accessTokenRepository.delete(dbtoken);
            userRepository.save(validUser);

            Cookie cookie = new Cookie("accessToken", null);
            cookie.setPath("/");
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);

            response.addCookie(cookie);

            refreshTokenRepository.deleteByEmail(adminEmail);
            return new ResponseEntity<>("admin logged out Successfully! ", HttpStatus.OK);
        }

        public ResponseEntity<String> resetPassword (String email){
            User validUser = userRepository.findByEmail(email);
            if (validUser == null) {
                return new ResponseEntity<>("No  User exists with this email!", HttpStatus.BAD_REQUEST);
            }
            if (validUser.getIsLocked() || !validUser.getIsActive()) {
                return new ResponseEntity<>("This user is either locked or inactive!", HttpStatus.BAD_REQUEST);
            }
            String resetToken = jwtUtilsService.generateToken(email, 15);
            validUser.setForgetPasswordToken(resetToken);
            userRepository.save(validUser);
            try {
                emailService.sendEmail(email, "Reset Password",
                        "Hit link to reset your password with password and confirm password as params: " + "/request-reset-password?token=" + resetToken);
            } catch (
                    MessagingException e) {
                return new ResponseEntity<>("Error sending Verification email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>("reset password mail sent", HttpStatus.OK);
        }

        public ResponseEntity<String> requestResetPassword (String token, @Valid String newPassword, @Valid String
        confirmNewPassword){
            String userEmail;
            User validUser;
            try {
                userEmail = jwtUtilsService.extractUsername(token);
            } catch (ExpiredJwtException e) {
                userEmail = e.getClaims().getSubject();
                validUser = userRepository.findByEmail(userEmail);
                validUser.setForgetPasswordToken(null);
                userRepository.save(validUser);
                return new ResponseEntity<>("Token expired!,change password request failed", HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                return new ResponseEntity<>("invalid token ,change password request failed!!", HttpStatus.BAD_REQUEST);
            }
            validUser = userRepository.findByEmail(userEmail);
            if (!token.equals(validUser.getForgetPasswordToken())) {
                return new ResponseEntity<>("incorrect token change password request failed!!", HttpStatus.BAD_REQUEST);
            }
            if (!newPassword.equals(confirmNewPassword)) {
                return new ResponseEntity<>("newpassword and confirmnewpassword didn't match change password request failed!!", HttpStatus.BAD_REQUEST);
            }
            validUser.setPassword(passwordEncoder.encode(newPassword));
            validUser.setForgetPasswordToken(null);

            validUser.setPasswordUpdateDate(LocalDateTime.now());
            userRepository.save(validUser);
            return new ResponseEntity<>("password Changed successfully!!", HttpStatus.OK);
        }
    }