package com.bootcamp.ecommerce_rohit.controllers.users;

import com.bootcamp.ecommerce_rohit.DTOs.SellerDTO;
import com.bootcamp.ecommerce_rohit.DTOs.SellerUpdateDTO;
import com.bootcamp.ecommerce_rohit.DTOs.UserAddressUpdateDTO;
import com.bootcamp.ecommerce_rohit.DTOs.UserPasswordUpdateDTO;
import com.bootcamp.ecommerce_rohit.services.SellerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@RestController
public class SellerController {
        @Autowired
        public SellerService sellerService;
        @PostMapping("/seller/signup")
        ResponseEntity<String> createSeller(@Valid @RequestBody SellerDTO sellerDTO, Locale locale){
            return sellerService.saveSeller(sellerDTO,locale);
}
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/seller/viewProfile")
    ResponseEntity<Map<String,Object>> viewSellerProfile(HttpServletRequest request,Locale locale){
        return sellerService.viewSellerProfile(request,locale);
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PutMapping(path="/seller/updateSellerProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> updateSellerProfile(HttpServletRequest request,@Valid  SellerUpdateDTO sellerUpdateDTO,Locale locale){
        return sellerService.updateSellerProfile(request,sellerUpdateDTO,locale);
    }
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PatchMapping("/seller/passwordUpdate")
    ResponseEntity<String> sellerPasswordUpdate(HttpServletRequest request,@Valid @RequestBody  UserPasswordUpdateDTO userPasswordUpdateDTO,Locale locale){
        return sellerService.sellerPasswordUpdate(request,userPasswordUpdateDTO,locale);
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PutMapping("/seller/addressUpdate")
    ResponseEntity<String> sellerAddressUpdate(HttpServletRequest request,@Valid @RequestBody  UserAddressUpdateDTO userAddressUpdateDTO
    ,@RequestParam String adressId,Locale locale){
        return sellerService.sellerAddressUpdate(request,userAddressUpdateDTO ,adressId,locale);
    }
}
