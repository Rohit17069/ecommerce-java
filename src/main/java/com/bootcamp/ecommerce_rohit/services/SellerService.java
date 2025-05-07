package com.bootcamp.ecommerce_rohit.services;

import com.bootcamp.ecommerce_rohit.DTOs.SellerDTO;
import com.bootcamp.ecommerce_rohit.DTOs.SellerUpdateDTO;
import com.bootcamp.ecommerce_rohit.DTOs.UserAddressUpdateDTO;
import com.bootcamp.ecommerce_rohit.DTOs.UserPasswordUpdateDTO;
import com.bootcamp.ecommerce_rohit.entities.*;
import com.bootcamp.ecommerce_rohit.repositories.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SellerService {
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    SellerRepository sellerRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtUtilsService jwtUtilsService;
    @Autowired
    AccessTokenRepository accessTokenRepository;
    @Autowired
    ImageUtils imageUtils;
    @Autowired
    MessageSource messageSource;

    public ResponseEntity<String> saveSeller(@Valid SellerDTO sellerDTO, Locale locale) {
        if (userRepository.findByEmail(sellerDTO.getEmail()) != null) {
            String message = messageSource.getMessage("email.exists", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        if (sellerRepository.findByGst(sellerDTO.getGst()) != null) {
            String message = messageSource.getMessage("gst.exists", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        if (!sellerDTO.getConfirmPassword().equals(sellerDTO.getPassword())) {
            String message = messageSource.getMessage("password.confirmPasswordMismatch", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        if (sellerRepository.findByCompanyName(sellerDTO.getCompanyName().toUpperCase()) != null) {
            String message = messageSource.getMessage("seller.companyName.exists", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        Seller seller = new Seller();
        seller.setFirstName(sellerDTO.getFirstName());
        seller.setLastName(sellerDTO.getLastName());
        seller.setEmail(sellerDTO.getEmail());
        seller.setCompanyContact(sellerDTO.getCompanyContact());
        seller.setPassword(passwordEncoder.encode(sellerDTO.getPassword()));
        Role role = roleRepository.findByAuthority("seller");
        seller.setRole(role);
        seller.setGst(sellerDTO.getGst());
        seller.setCompanyName(sellerDTO.getCompanyName().toUpperCase());
        List<Address> newAddresses = seller.getAddresses();
        newAddresses.add(sellerDTO.getCompanyAddress());
        seller.setAddresses(newAddresses);
        seller.setIsDeleted(false);
        seller.setPasswordUpdateDate(LocalDateTime.now());
        userRepository.save(seller);
        try {
            emailService.sendEmail("rohit.gupta1@tothenew.com", "Seller account created,approval pending",
                    "Seller Account created,Waiting for Approval");
            String message = messageSource.getMessage("seller.creation.success", null, locale);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (MessagingException e) {
            String message = messageSource.getMessage("email.error", null, locale);
            return new ResponseEntity<>(message + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, Object>> viewSellerProfile(HttpServletRequest request, Locale locale) {
        Map<String, Object> sellerProfile = new LinkedHashMap<>();
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }
        String sellerEmail = jwtUtilsService.extractUsername(token);
        Seller seller = sellerRepository.findByEmail(sellerEmail);
        sellerProfile.put("Id", seller.getId());
        sellerProfile.put("First Name", seller.getFirstName());
        sellerProfile.put("Middle Name", seller.getMiddleName());
        sellerProfile.put("Last Name", seller.getLastName());
        sellerProfile.put("is_Active", seller.getIsActive());
        sellerProfile.put("Company Contact Number", seller.getCompanyContact());
        sellerProfile.put("Company Name", seller.getCompanyName());
        sellerProfile.put("GST", seller.getGst());
        sellerProfile.put("Address", seller.getAddresses().getFirst());
        sellerProfile.put("profileImageUrl", imageUtils.getImageURL(seller.getId(),"users"));
        return new ResponseEntity<>(sellerProfile, HttpStatus.OK);
    }

    public ResponseEntity<String> updateSellerProfile(HttpServletRequest request, @Valid SellerUpdateDTO sellerUpdateDTO, Locale locale) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }

        String sellerEmail = jwtUtilsService.extractUsername(token);
        Seller seller = sellerRepository.findByEmail(sellerEmail);

        if (sellerUpdateDTO.getCompanyContact() != null
                && !sellerUpdateDTO.getCompanyContact().equals(seller.getCompanyContact())) {
            if (sellerRepository.existsByCompanyContact(sellerUpdateDTO.getCompanyContact())) {
                String message = messageSource.getMessage("contact.exists", null, locale);
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
            }
            seller.setCompanyContact(sellerUpdateDTO.getCompanyContact());
        }

        if (sellerUpdateDTO.getCompanyName() != null
                && !sellerUpdateDTO.getCompanyName().equalsIgnoreCase(seller.getCompanyName())) {
            if (sellerRepository.existsByCompanyNameIgnoreCase(sellerUpdateDTO.getCompanyName())) {
                String message = messageSource.getMessage("seller.companyName.exists", null, locale);
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
            }
            seller.setCompanyName(sellerUpdateDTO.getCompanyName());
        }

        if (sellerUpdateDTO.getGst() != null
                && !sellerUpdateDTO.getGst().equalsIgnoreCase(seller.getGst())) {
            if (sellerRepository.existsByGst(sellerUpdateDTO.getGst())) {
                String message = messageSource.getMessage("gst.exists", null, locale);
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
            }
            seller.setGst(sellerUpdateDTO.getGst());
        }

        if (sellerUpdateDTO.getFirstName() != null) {
            seller.setFirstName(sellerUpdateDTO.getFirstName());
        }

        if (sellerUpdateDTO.getLastName() != null) {
            seller.setLastName(sellerUpdateDTO.getLastName());
        }

        if (sellerUpdateDTO.getMiddleName() != null) {
            seller.setMiddleName(sellerUpdateDTO.getMiddleName());
        }
        if (sellerUpdateDTO.getProfileImage() != null) {
            imageUtils.saveImageOnServer(sellerUpdateDTO.getProfileImage(), seller.getId(),"users");
        }
        sellerRepository.save(seller);
        String message = messageSource.getMessage("seller.profile.update.success", null, locale);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    public ResponseEntity<String> sellerPasswordUpdate(HttpServletRequest request, @Valid UserPasswordUpdateDTO sellerPasswordUpdateDTO, Locale locale) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }
        String sellerEmail = jwtUtilsService.extractUsername(token);
        Seller seller = sellerRepository.findByEmail(sellerEmail);
        if (!sellerPasswordUpdateDTO.getNewPassword().equals(sellerPasswordUpdateDTO.getConfirmNewPassword())) {
            String message = messageSource.getMessage("password.mismatch", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        seller.setPassword(passwordEncoder.encode(sellerPasswordUpdateDTO.getNewPassword()));
        seller.setPasswordUpdateDate(LocalDateTime.now());
        try {
            emailService.sendEmail(sellerEmail, "Seller password updated",
                    "seller password updated successfully");
        } catch (
                MessagingException e) {
            return new ResponseEntity<>("Error sending Verification email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        sellerRepository.save(seller);
        String message = messageSource.getMessage("password.update.success", null, locale);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    public ResponseEntity<String> sellerAddressUpdate(HttpServletRequest request, @Valid UserAddressUpdateDTO userAddressUpdateDTO, String addressId, Locale locale) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }
        String sellerEmail = jwtUtilsService.extractUsername(token);
        Seller seller = sellerRepository.findByEmail(sellerEmail);
        Address requestAddress = addressRepository.findById(addressId);
        if (requestAddress == null || requestAddress.getIs_Deleted() == true) {
            String message = messageSource.getMessage("address.notfound", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        if (!(requestAddress.getId().equals(seller.getAddresses().getFirst().getId()))) {
            String message = messageSource.getMessage("address.unauthorized.update", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        if (userAddressUpdateDTO.getCity() != null) {
            requestAddress.setCity(userAddressUpdateDTO.getCity());
        }
        if (userAddressUpdateDTO.getState() != null) {
            requestAddress.setState(userAddressUpdateDTO.getState());
        }
        if (userAddressUpdateDTO.getCountry() != null) {
            requestAddress.setCountry(userAddressUpdateDTO.getCountry());
        }
        if (userAddressUpdateDTO.getAddressLine() != null) {
            requestAddress.setAddressLine(userAddressUpdateDTO.getAddressLine());
        }
        if (userAddressUpdateDTO.getZipCode() != null) {
            requestAddress.setZipCode(userAddressUpdateDTO.getZipCode());
        }
        if (userAddressUpdateDTO.getLabel() != null) {
            requestAddress.setLabel(userAddressUpdateDTO.getLabel());
        }
        addressRepository.save(requestAddress);
        String message = messageSource.getMessage("address.update.success", null, locale);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}