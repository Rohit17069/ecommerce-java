package com.bootcamp.ecommerce_rohit.services;

import com.bootcamp.ecommerce_rohit.DTOs.CustomerDTO;
import com.bootcamp.ecommerce_rohit.DTOs.CustomerUpdateDTO;
import com.bootcamp.ecommerce_rohit.DTOs.UserAddressUpdateDTO;
import com.bootcamp.ecommerce_rohit.DTOs.UserPasswordUpdateDTO;
import com.bootcamp.ecommerce_rohit.entities.Address;
import com.bootcamp.ecommerce_rohit.entities.Customer;
import com.bootcamp.ecommerce_rohit.entities.Role;
import com.bootcamp.ecommerce_rohit.entities.User;
import com.bootcamp.ecommerce_rohit.repositories.AddressRepository;
import com.bootcamp.ecommerce_rohit.repositories.CustomerRepository;
import com.bootcamp.ecommerce_rohit.repositories.RoleRepository;
import com.bootcamp.ecommerce_rohit.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomerService {
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtilsService jwtUtilsService;
    @Autowired
    EmailService emailService;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private MessageSource messageSource;

    @Autowired
    ImageUtils imageUtils;

    public ResponseEntity<String> saveCustomer(@Valid CustomerDTO customerDTO, Locale locale) {
        log.info("Saving customer with email: {}", customerDTO.getEmail());

        if (userRepository.findByEmail(customerDTO.getEmail()) != null) {
            log.warn("Email already exists: {}", customerDTO.getEmail());
            String message = messageSource.getMessage("email.exists", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        if (!customerDTO.getConfirmPassword().equals(customerDTO.getPassword())) {
            log.warn("Password and Confirm Password mismatch for email: {}", customerDTO.getEmail());
            String message = messageSource.getMessage("password.confirmPasswordMismatch", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        Customer customer = new Customer();
        customer.setAddresses(customerDTO.getAddresses());
        customer.setFirstName(customerDTO.getFirstName());
        customer.setLastName(customerDTO.getLastName());
        customer.setEmail(customerDTO.getEmail());
        customer.setContact(customerDTO.getContact());
        customer.setMiddleName(customerDTO.getMiddleName());
        customer.setPassword(passwordEncoder.encode(customerDTO.getPassword()));
        String token = jwtUtilsService.generateToken(customerDTO.getEmail(), 3 * 60);
        Role role = roleRepository.findByAuthority("customer");
        customer.setRole(role);
        customer.setRegistrationAndLoginToken(token);
        customer.setIsDeleted(false);
        customer.setPasswordUpdateDate(LocalDateTime.now());
        userRepository.save(customer);

        try {
            log.info("Sending verification email to: {}", customerDTO.getEmail());
            String message1 = messageSource.getMessage("customer.EmailVerificationSuccess", null, locale);
            String message2 = messageSource.getMessage("email.hit", null, locale);
            emailService.sendEmail(customerDTO.getEmail(), message1,
                    message2 + "/customer/register-verification?token=" + token);
            String message3 = messageSource.getMessage("customer.creationSuccessfull", null, locale);
            return new ResponseEntity<>(message3, HttpStatus.OK);
        } catch (MessagingException e) {
            log.error("Error sending verification email: {}", e.getMessage(), e);
            String message = messageSource.getMessage("email.error", null, locale);
            return new ResponseEntity<>(message + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> validateToken(String token, Locale locale) throws MessagingException {
        log.info("Validating token for customer");

        String customerEmail;
        try {
            customerEmail = jwtUtilsService.extractUsername(token);
        } catch (ExpiredJwtException e) {
            Claims expiredClaims = e.getClaims();
            customerEmail = expiredClaims.getSubject();
            String newToken = jwtUtilsService.generateToken(customerEmail, 3 * 60);
            String message1 = messageSource.getMessage("customer.tokenExpired", null, locale);
            log.warn("Token expired for customer: {}, sending new token", customerEmail);
            emailService.sendEmail(customerEmail, message1,
                    message1 + ": " + "/customer/register-verification?token=" + newToken);
            String message2 = messageSource.getMessage("token.expired", null, locale);
            return new ResponseEntity<>(message2, HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            log.error("Invalid token error: {}", e.getMessage(), e);
            String message3 = messageSource.getMessage("token.invalid", null, locale);
            return new ResponseEntity<>(message3, HttpStatus.BAD_REQUEST);
        }

        User validUser = userRepository.findByEmail(customerEmail);
        if (validUser == null) {
            log.warn("No customer found for email extracted from token: {}", customerEmail);
            String message4 = messageSource.getMessage("customer.notfound", null, locale);
            return new ResponseEntity<>(message4, HttpStatus.BAD_REQUEST);
        }
        if (validUser.getIsActive()) {
            log.info("Customer already verified: {}", validUser.getEmail());
            return new ResponseEntity<>("Customer already verified", HttpStatus.BAD_REQUEST);
        }
        validUser.setIsActive(true);
        validUser.setRegistrationAndLoginToken(null);

        userRepository.save(validUser);
        try {
            log.info("Sending account activation email to: {}", validUser.getEmail());
            emailService.sendEmail(validUser.getEmail(), "Account activated",
                    "account activated");
        } catch (MessagingException e) {
            log.error("Error sending account activation email: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error sending Verification email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String message5 = messageSource.getMessage("customer.verified", null, locale);
        return new ResponseEntity<>(message5, HttpStatus.OK);
    }

    public ResponseEntity<String> resendVerification(String email, Locale locale) {
        log.info("Resend verification requested for email: {}", email);

        User dbCustomer = userRepository.findByEmail(email);
        if (dbCustomer == null) {
            log.warn("No customer found for resend verification: {}", email);
            String message1 = messageSource.getMessage("resend.notfound", null, locale);
            return new ResponseEntity<>(message1, HttpStatus.NOT_FOUND);
        }
        if (dbCustomer.getIsActive()) {
            log.info("Customer already active, no need to resend: {}", email);
            String message2 = messageSource.getMessage("resend.already", null, locale);
            return new ResponseEntity<>(message2, HttpStatus.BAD_REQUEST);
        }

        String resendToken = jwtUtilsService.generateToken(email, 3 * 60);
        User user = userRepository.findByEmail(email);
        try {
            log.info("Sending resend verification email to: {}", email);
            String message3 = messageSource.getMessage("resend.resent", null, locale);
            String message4 = messageSource.getMessage("email.hit", null, locale);
            emailService.sendEmail(email, message3,
                    message4 + "/customer/register-verification?token=" + resendToken);
            String message5 = messageSource.getMessage("resend.success", null, locale);
            return new ResponseEntity<>(message5, HttpStatus.OK);
        } catch (MessagingException e) {
            log.error("Error sending resend verification email: {}", e.getMessage(), e);
            String message6 = messageSource.getMessage("email.error", null, locale);
            return new ResponseEntity<>(message6 + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, Object>> viewCustomerProfile(HttpServletRequest request) {
        log.info("Fetching customer profile from request cookies");

        Map<String, Object> customerProfile = new LinkedHashMap<>();
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }
        String customerEmail = jwtUtilsService.extractUsername(token);
        Customer customer = customerRepository.findByEmail(customerEmail);
        customerProfile.put("Id", customer.getId());
        customerProfile.put("First Name", customer.getFirstName());
        customerProfile.put("Middle Name", customer.getMiddleName());
        customerProfile.put("Last Name", customer.getLastName());
        customerProfile.put("is_Active", customer.getIsActive());
        customerProfile.put(" Contact Number", customer.getContact());
        customerProfile.put("profileImageUrl", imageUtils.getImageURL(customer.getId(), "users"));
        log.info("Customer profile fetched successfully for email: {}", customerEmail);
        return new ResponseEntity<>(customerProfile, HttpStatus.OK);
    }

    public ResponseEntity<List<Address>> viewCustomerAddresses(HttpServletRequest request) {
        List<Address> customerAddresses = new ArrayList<>();
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }
        String customerEmail = jwtUtilsService.extractUsername(token);
        Customer customer = customerRepository.findByEmail(customerEmail);
        List<Address> addresses = customer.getAddresses();
        addresses = addresses.stream().filter(address -> !address.getIs_Deleted().booleanValue()).collect(Collectors.toList());
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    public ResponseEntity<String> updateCustomerProfile(HttpServletRequest request, @Valid CustomerUpdateDTO customerUpdateDTO, Locale locale) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }
        String customerEmail = jwtUtilsService.extractUsername(token);
        Customer customer = customerRepository.findByEmail(customerEmail);
        if (customerUpdateDTO.getFirstName() != null) {
            customer.setFirstName(customerUpdateDTO.getFirstName());
        }
        if (customerUpdateDTO.getLastName() != null) {
            customer.setLastName(customerUpdateDTO.getLastName());
        }
        if (customerUpdateDTO.getMiddleName() != null) {
            customer.setMiddleName(customerUpdateDTO.getMiddleName());
        }
        if (customerUpdateDTO.getContact() != null
                && !customerUpdateDTO.getContact().equals(customer.getContact())) {

            if (customerRepository.existsByContact(customerUpdateDTO.getContact())) {
                String message1 = messageSource.getMessage("contact.exists", null, locale);
                return new ResponseEntity<>(message1, HttpStatus.BAD_REQUEST);
            }

            customer.setContact(customerUpdateDTO.getContact());
        }
        if (customerUpdateDTO.getProfileImage() != null) {
            imageUtils.saveImageOnServer(customerUpdateDTO.getProfileImage(), customer.getId(), "users");
        }

        customerRepository.save(customer);
        String message2 = messageSource.getMessage("profile.update.success", null, locale);
        return new ResponseEntity<>(message2, HttpStatus.OK);
    }

    public ResponseEntity<String> customerPasswordUpdate(HttpServletRequest request, @Valid UserPasswordUpdateDTO customerPasswordUpdateDTO, Locale locale) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }
        String customerEmail = jwtUtilsService.extractUsername(token);
        Customer customer = customerRepository.findByEmail(customerEmail);
        if (!customerPasswordUpdateDTO.getNewPassword().equals(customerPasswordUpdateDTO.getConfirmNewPassword())) {
            String message = messageSource.getMessage("password.mismatch", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        customer.setPassword(passwordEncoder.encode(customerPasswordUpdateDTO.getNewPassword()));
        customer.setPasswordUpdateDate(LocalDateTime.now());
        try {
            emailService.sendEmail(customerEmail, "Customer password updated",
                    "customer password updated successfully");
        } catch (
                MessagingException e) {
            return new ResponseEntity<>("Error sending Verification email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

    }
        customerRepository.save(customer);
        String message = messageSource.getMessage("password.update.success", null, locale);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    public ResponseEntity<String> customerAddAddress(HttpServletRequest request, @Valid @RequestBody UserAddressUpdateDTO userAddressUpdateDTO, Locale locale) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }

        String customerEmail = jwtUtilsService.extractUsername(token);
        Customer customer = customerRepository.findByEmail(customerEmail);
        Address address = new Address();
        if (userAddressUpdateDTO.getCity() != null) {
            address.setCity(userAddressUpdateDTO.getCity());
        }
        if (userAddressUpdateDTO.getState() != null) {
            address.setState(userAddressUpdateDTO.getState());
        }
        if (userAddressUpdateDTO.getCountry() != null) {
            address.setCountry(userAddressUpdateDTO.getCountry());
        }
        if (userAddressUpdateDTO.getAddressLine() != null) {
            address.setAddressLine(userAddressUpdateDTO.getAddressLine());
        }
        if (userAddressUpdateDTO.getLabel() != null) {
            address.setLabel(userAddressUpdateDTO.getLabel());
        }
        if (userAddressUpdateDTO.getZipCode() != null) {
            address.setZipCode(userAddressUpdateDTO.getZipCode());
        }
        address.setIs_Deleted(false);
        List<Address> newAddresses = customer.getAddresses();
        newAddresses.add(address);
        customer.setAddresses(newAddresses);
        addressRepository.save(address);
        customerRepository.save(customer);
        String message = messageSource.getMessage("address.add.success", null, locale);
        return new ResponseEntity<>(message, HttpStatus.OK);

    }


    public ResponseEntity<String> customerDeleteAddress(HttpServletRequest request, String addressId, Locale locale) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }
        Address address = addressRepository.findById(addressId);
        if (address == null) {
            String message = messageSource.getMessage("address.notfound", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        ;
        String customerEmail = jwtUtilsService.extractUsername(token);
        Customer customer = customerRepository.findByEmail(customerEmail);
        if (customer.getAddresses().stream().noneMatch(customeraddress -> Objects.equals(customeraddress.getId(), address.getId()))) {
            String message = messageSource.getMessage("address.unauthorized.delete", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        ;
        address.setIs_Deleted(true);
//        customer.getAddresses().remove(address);
        addressRepository.save(address);
//        customerRepository.save(customer);
        String message = messageSource.getMessage("address.delete.success", null, locale);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    public ResponseEntity<String> customerAddressUpdate(HttpServletRequest request, @Valid UserAddressUpdateDTO userAddressUpdateDTO
            , String addressId, Locale locale) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }
        String customerEmail = jwtUtilsService.extractUsername(token);
        Customer customer = customerRepository.findByEmail(customerEmail);
        Address requestAddress = addressRepository.findById(addressId);
        if (requestAddress == null || requestAddress.getIs_Deleted() == true) {
            String message = messageSource.getMessage("address.notfound", null, locale);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        ;
        if (customer.getAddresses().stream().noneMatch(address -> address.getId().equals(requestAddress.getId()))) {
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

