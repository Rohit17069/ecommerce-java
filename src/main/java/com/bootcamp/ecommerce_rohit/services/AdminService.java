package com.bootcamp.ecommerce_rohit.services;

import com.bootcamp.ecommerce_rohit.DTOs.CustomerResponseDTO;
import com.bootcamp.ecommerce_rohit.DTOs.SellerResponseDTO;
import com.bootcamp.ecommerce_rohit.entities.Customer;
import com.bootcamp.ecommerce_rohit.entities.Seller;
import com.bootcamp.ecommerce_rohit.exceptionsHandling.PaginationError;
import com.bootcamp.ecommerce_rohit.repositories.CustomerRepository;
import com.bootcamp.ecommerce_rohit.repositories.SellerRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class AdminService {
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    SellerRepository sellerRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    MessageSource messageSource;

    public List<CustomerResponseDTO> getCustomers(Integer pageOffset, Integer pageSize, String sortBy, String emailFilterValue) {
        List<CustomerResponseDTO> customersList = new ArrayList<>();
        Page<Customer> page;
        List<Customer> customers;
        Integer sizee = customerRepository.findAll().size();
        if (pageOffset < 0 || (double) pageOffset >= (double) sizee / (double) pageSize ) {
            throw new PaginationError("invalid pageOffset");

        }

        Pageable pageable = PageRequest.of(pageOffset, pageSize);
        if (emailFilterValue == null) {
            page = customerRepository.findAll(pageable);
        } else {
            page = customerRepository.findByEmail(emailFilterValue, pageable);
        }

        customers = page.getContent();
        for (Customer c : customers) {
            CustomerResponseDTO responseCustomer = new CustomerResponseDTO();
            responseCustomer.setId(c.getId());
            responseCustomer.setEmail(c.getEmail());
            responseCustomer.setFullName(c.getFirstName() + c.getMiddleName() + c.getLastName());
            responseCustomer.setIsActive(c.getIsActive());
            responseCustomer.setPhoneNumber(c.getContact());
            customersList.add(responseCustomer);
        }

        if (sortBy.equals("id")) {
            customersList.sort(Comparator.comparing(CustomerResponseDTO::getId));
        }

        if (sortBy.equals("email")) {
            customersList.sort(Comparator.comparing(CustomerResponseDTO::getEmail));
        }

        if (sortBy.equals("fullname")) {
            customersList.sort(Comparator.comparing(CustomerResponseDTO::getFullName));
        }
        return customersList;
    }

    public List<SellerResponseDTO> getSellers(Integer pageOffset, Integer pageSize, String sortBy, String emailFilterValue) {
        List<SellerResponseDTO> sellersList = new ArrayList<>();
        Page<Seller> page;
        List<Seller> sellers;
        Integer sizee = customerRepository.findAll().size();
        if (pageOffset < 0 || (double) pageOffset >= (double) sizee / (double) pageSize) {
            throw new PaginationError("invalid pageOffset");
        }
        Pageable pageable = PageRequest.of(pageOffset, pageSize);
        if (emailFilterValue == null) {
            page = sellerRepository.findAll(pageable);
        } else {
            page = sellerRepository.findByEmail(emailFilterValue, pageable);
        }

        sellers = page.getContent();
        for (Seller s : sellers) {
            SellerResponseDTO responseSeller = new SellerResponseDTO();
            responseSeller.setId(s.getId());
            responseSeller.setEmail(s.getEmail());
            if (s.getMiddleName() != null) {
                responseSeller.setFullName(s.getFirstName() + s.getMiddleName() + s.getLastName());
            } else {
                responseSeller.setFullName(s.getFirstName() + s.getLastName());
            }
            responseSeller.setIsActive(s.getIsActive());
            responseSeller.setCompanyName(s.getCompanyName());
            responseSeller.setCompanyContact(s.getCompanyContact());
            responseSeller.setCompanyAddress(s.getAddresses().getFirst());
            sellersList.add(responseSeller);
        }

        if (sortBy.equals("id")) {
            sellersList.sort(Comparator.comparing(SellerResponseDTO::getId));
        }
        if (sortBy.equals("companyName")) {
            sellersList.sort(Comparator.comparing(SellerResponseDTO::getCompanyName));
        }
        if (sortBy.equals("companyContact")) {
            sellersList.sort(Comparator.comparing(SellerResponseDTO::getCompanyContact));
        }
        if (sortBy.equals("email")) {
            sellersList.sort(Comparator.comparing(SellerResponseDTO::getEmail));
        }

        if (sortBy.equals("fullname")) {
            sellersList.sort(Comparator.comparing(SellerResponseDTO::getFullName));
        }
        return sellersList;
    }

    public ResponseEntity<String> activateCustomer(String customerId, Locale locale) {
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            String message = messageSource.getMessage("customer.notfound", null, locale);
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        if (customer.getIsActive()) {
            String message = messageSource.getMessage("customer.already.activated", null, locale);
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        customer.setIsActive(true);
        customerRepository.save(customer);
        String customerEmail = customer.getEmail();
        try {
            emailService.sendEmail(customerEmail, messageSource.getMessage("customer.activation.email.subject", null, locale),
                    messageSource.getMessage("customer.activation.email.body", null, locale));
            String message = messageSource.getMessage("customer.activation.success", null, locale);
            return new ResponseEntity<>(message, HttpStatus.OK);

        } catch (MessagingException e) {
            String message = messageSource.getMessage("email.error", null, locale);
            return new ResponseEntity<>(message + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> deactivateCustomer(String customerId, Locale locale) {
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            String message = messageSource.getMessage("customer.notfound", null, locale);
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        if (!customer.getIsActive()) {
            String message = messageSource.getMessage("customer.already.deactivated", null, locale);
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        customer.setIsActive(false);
        customerRepository.save(customer);
        String customerEmail = customer.getEmail();
        try {
            emailService.sendEmail(customerEmail, messageSource.getMessage("customer.deactivation.email.subject", null, locale),
                    messageSource.getMessage("customer.deactivation.email.body", null, locale));
            String message = messageSource.getMessage("customer.deactivation.success", null, locale);
            return new ResponseEntity<>(message, HttpStatus.OK);

        } catch (MessagingException e) {
            String message = messageSource.getMessage("email.error", null, locale);
            return new ResponseEntity<>(message + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> activateSeller(String sellerId, Locale locale) {
        Seller seller = sellerRepository.findById(sellerId);
        if (seller == null) {
            String message = messageSource.getMessage("seller.notfound", null, locale);
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        if (seller.getIsActive()) {
            String message = messageSource.getMessage("seller.already.activated", null, locale);
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        seller.setIsActive(true);
        sellerRepository.save(seller);
        String sellerEmail = seller.getEmail();
        try {
            emailService.sendEmail(sellerEmail, messageSource.getMessage("seller.activation.email.subject", null, locale),
                    messageSource.getMessage("seller.activation.email.body", null, locale));
            String message = messageSource.getMessage("seller.activation.success", null, locale);
            return new ResponseEntity<>(message, HttpStatus.OK);

        } catch (MessagingException e) {
            String message = messageSource.getMessage("email.error", null, locale);
            return new ResponseEntity<>(message + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> deactivateSeller(String sellerId, Locale locale) {
        Seller seller = sellerRepository.findById(sellerId);
        if (seller == null) {
            String message = messageSource.getMessage("seller.notfound", null, locale);
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        if (!seller.getIsActive()) {
            String message = messageSource.getMessage("seller.already.deactivated", null, locale);
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        seller.setIsActive(false);
        sellerRepository.save(seller);
        String sellerEmail = seller.getEmail();
        try {
            emailService.sendEmail(sellerEmail, messageSource.getMessage("seller.deactivation.email.subject", null, locale),
                    messageSource.getMessage("seller.deactivation.email.body", null, locale));
            String message = messageSource.getMessage("seller.deactivation.success", null, locale);
            return new ResponseEntity<>(message, HttpStatus.OK);

        } catch (MessagingException e) {
            String message = messageSource.getMessage("email.error", null, locale);
            return new ResponseEntity<>(message + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}