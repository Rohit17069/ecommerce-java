package com.bootcamp.ecommerce_rohit.controllers.users;


import com.bootcamp.ecommerce_rohit.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/login/customer")
    ResponseEntity<String> customerLogin(@RequestBody Map<String, String> map, HttpServletResponse response,
                                         HttpServletRequest request) {
        return authService.userLogin(map.get("email"), map.get("password"), response, request);
    }

    @PostMapping("/login/seller")
    ResponseEntity<String> sellerLogin(@RequestBody Map<String, String> map, HttpServletResponse response,
                                       HttpServletRequest request) {
        return authService.userLogin(map.get("email"), map.get("password"), response, request);
    }

    @PostMapping("/login/admin")
    ResponseEntity<String> adminLogin(@RequestBody Map<String, String> map, HttpServletResponse response,
                                      HttpServletRequest request) {
        return authService.userLogin(map.get("email"), map.get("password"), response, request);
    }
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping("/logout/customer")
    ResponseEntity<String> customerLogout(HttpServletRequest request,HttpServletResponse response) {
        return authService.customerLogout(request, response);
    }
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PostMapping("/logout/seller")
    ResponseEntity<String> sellerLogout(HttpServletRequest request,HttpServletResponse response) {
        return authService.sellerLogout(request, response);
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/logout/admin")
    ResponseEntity<String> adminLogout(HttpServletRequest request,HttpServletResponse response) {
        return authService.adminLogout(request, response);
    }

    @PostMapping("/reset-password")
    ResponseEntity<String> resetPassword(@RequestParam String email) {
        return authService.resetPassword(email);
    }

    @PatchMapping("request-resetpassword")
    ResponseEntity<String> requestResetPassword(@RequestParam String token, @RequestParam String newPassword, @RequestParam
    String confirmNewPassword) {
        return authService.requestResetPassword(token, newPassword, confirmNewPassword);
    }

}
