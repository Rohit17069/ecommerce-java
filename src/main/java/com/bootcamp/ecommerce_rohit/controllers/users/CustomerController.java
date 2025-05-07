package com.bootcamp.ecommerce_rohit.controllers.users;

import com.bootcamp.ecommerce_rohit.DTOs.*;
import com.bootcamp.ecommerce_rohit.entities.Address;
import com.bootcamp.ecommerce_rohit.entities.Customer;
import com.bootcamp.ecommerce_rohit.entities.User;
import com.bootcamp.ecommerce_rohit.repositories.UserRepository;
import com.bootcamp.ecommerce_rohit.services.CustomerService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
public class CustomerController {

    @Autowired
    public CustomerService customerService;

    @PostMapping("/customer/signup")
    ResponseEntity<String> createCustomer(@Valid @RequestBody CustomerDTO customerDTO, Locale locale) {
        return customerService.saveCustomer(customerDTO,locale);
    }

    @PutMapping("/customer/register-verification")
    ResponseEntity<String> customerEmailVerification(@RequestParam String token,Locale locale) throws MessagingException {
        return customerService.validateToken(token,locale);
    }

    @PostMapping("/customer/resend-verification")
    ResponseEntity<String> customerResendVerificationMail(@RequestParam String email,Locale locale) {
        return customerService.resendVerification(email,locale);
    }
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/customer/viewProfile")
    ResponseEntity<Map<String,Object>> viewCustomerProfile(HttpServletRequest request){
        return customerService.viewCustomerProfile(request);
    }
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/customer/viewAddresses")
    ResponseEntity<List<Address>> viewCustomerAddresses(HttpServletRequest request){
        return customerService.viewCustomerAddresses(request);
    }
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PutMapping(value = "/customer/updateCustomerProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> updateCustomerProfile(HttpServletRequest request,@ModelAttribute @Valid CustomerUpdateDTO customerUpdateDTO,Locale locale){
        return customerService.updateCustomerProfile(request,customerUpdateDTO,locale);
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PatchMapping("/customer/passwordUpdate")
    ResponseEntity<String> customerPasswordUpdate(HttpServletRequest request,@Valid @RequestBody UserPasswordUpdateDTO userPasswordUpdateDTO,Locale locale){
        return customerService.customerPasswordUpdate(request,userPasswordUpdateDTO,locale);
    }
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping("/customer/addAddress")
    ResponseEntity<String> customerAddAddress(HttpServletRequest request,@Valid @RequestBody UserAddressUpdateDTO userAddressUpdateDTO,Locale locale){
        return customerService.customerAddAddress(request,userAddressUpdateDTO,locale);
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @DeleteMapping("/customer/deleteAddress")
    ResponseEntity<String> deleteAddress(HttpServletRequest request,@RequestParam String addressId,Locale locale){
        return customerService.customerDeleteAddress(request,addressId,locale);
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PutMapping("/customer/addressUpdate")
    ResponseEntity<String> customerAddressUpdate(HttpServletRequest request,@Valid @RequestBody  UserAddressUpdateDTO userAddressUpdateDTO
            ,@RequestParam String adressId,Locale locale){
        return customerService.customerAddressUpdate(request,userAddressUpdateDTO ,adressId,locale);
    }
};

