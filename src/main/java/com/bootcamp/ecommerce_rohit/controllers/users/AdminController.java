package com.bootcamp.ecommerce_rohit.controllers.users;
import com.bootcamp.ecommerce_rohit.DTOs.CustomerResponseDTO;
import com.bootcamp.ecommerce_rohit.DTOs.SellerResponseDTO;
import com.bootcamp.ecommerce_rohit.services.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
public class AdminController {

    @Autowired
    AdminService adminService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/getCustomers")
    List<CustomerResponseDTO> getCustomers(
            @RequestParam(required = false, defaultValue = "10") Integer pageSize, @RequestParam(required = false, defaultValue = "0") Integer pageOffset,
                                           @RequestParam(required = false, defaultValue = "id") String sortBy, @RequestParam(required = false) String emailFilterValue) {
        return adminService.getCustomers(pageOffset, pageSize, sortBy, emailFilterValue);
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/getSellers")
    List<SellerResponseDTO> getSellers(
            @RequestParam(required = false, defaultValue = "10") Integer pageSize, @RequestParam(required = false, defaultValue = "0") Integer pageOffset,
            @RequestParam(required = false, defaultValue = "id") String sortBy, @RequestParam(required = false) String emailFilterValue) {
        return adminService.getSellers(pageOffset, pageSize, sortBy, emailFilterValue);

    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/activateCustomer")
    ResponseEntity<String> activateCustomer(@RequestParam String customerId, Locale locale){
        return adminService.activateCustomer(customerId,locale);
    };

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/deactivateCustomer")
    ResponseEntity<String> deactivateCustomer(@RequestParam String customerId,Locale locale){
        return adminService.deactivateCustomer(customerId,locale);
    };
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/activateSeller")
    ResponseEntity<String> activateSeller(@RequestParam String sellerId,Locale locale){
        return adminService.activateSeller(sellerId,locale);
    };

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/deactivateSeller")
    ResponseEntity<String> deactivateSeller(@RequestParam String sellerId,Locale locale){
        return adminService.deactivateSeller(sellerId,locale);
    };
}


