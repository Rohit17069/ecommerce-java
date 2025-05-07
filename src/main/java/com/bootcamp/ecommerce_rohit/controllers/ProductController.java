package com.bootcamp.ecommerce_rohit.controllers;

import com.bootcamp.ecommerce_rohit.DTOs.*;
import com.bootcamp.ecommerce_rohit.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Locale;

@RestController
public class ProductController {
    @Autowired
    ProductService productService;
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PostMapping("/addProduct")
    ResponseEntity<String> addProduct(@Valid @RequestBody AddProductDTO addProductDTO, Principal principal, Locale locale){
        return productService.addProduct(addProductDTO,principal,locale);
    };
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PostMapping(value = "/addProductVariation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> addProductVariation(@ModelAttribute
            @Valid AddProductVariationDTO addProductVariationDTO, Principal principal, Locale locale) throws IOException {
        return productService.addProductVariation(addProductVariationDTO,principal,locale);
    }
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/viewProductSeller")
    ResponseEntity<ViewProductSellerDTO> viewProductSeller(@RequestParam String productId, Principal principal, Locale locale){
        return productService.viewProductSeller(productId,principal,locale);
    };
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/viewProductVariation")
    ResponseEntity<ViewProductVariationDTO> viewProductVariation(@RequestParam String productVariationId, Principal principal, Locale locale){
        return productService.viewProductVariation(productVariationId,principal,locale);
    };

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/viewAllProductsSeller")
    ResponseEntity<List<ViewProductSellerDTO>> viewAllProductsSeller(@RequestParam(required = false, defaultValue = "10") Integer pageSize, @RequestParam(required = false, defaultValue = "0") Integer pageOffset,
                                                               @RequestParam(required = false, defaultValue = "id") String sortBy, @RequestParam(required = false,defaultValue = "desc") String order, @RequestParam(required = false) String query, Principal principal, Locale locale){
        return productService.viewAllProductsSeller(pageSize,pageOffset,sortBy,order,query,principal,locale);
    };
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/viewAllProductVariations")
    ResponseEntity<List<ViewProductVariationDTO>> viewAllProductVariations(@RequestParam String productId,@RequestParam(required = false, defaultValue = "10") Integer pageSize, @RequestParam(required = false, defaultValue = "0") Integer pageOffset,
                                                           @RequestParam(required = false, defaultValue = "id") String sortBy,@RequestParam(required = false,defaultValue = "desc") String order, @RequestParam(required = false) String query, Principal principal, Locale locale){
        return productService.viewAllProductVariations(productId,pageSize,pageOffset,sortBy,order,query,principal,locale);
    };

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @DeleteMapping("/deleteProduct")
    ResponseEntity<String> deleteProduct(String productId, Principal principal, Locale locale){
        return productService.deleteProduct(productId,principal,locale);
    };
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PutMapping("/updateProduct")
    ResponseEntity<String> updateProduct(@RequestBody @Valid UpdateProductDTO updateProductDTO, Principal principal, Locale locale){
        return productService.updateProduct(updateProductDTO,principal,locale);
    };
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PutMapping(value = "/updateProductVariation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> updateProductVariation(@ModelAttribute @Valid UpdateProductVariationDTO updateProductVariationDTO, Principal principal, Locale locale){
        return productService.updateProductVariation(updateProductVariationDTO,principal,locale);
    };
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/viewProductCustomer")
    ResponseEntity<ViewProductCustomerDTO> viewProductCustomer(String productId, Locale locale){
        return productService.viewProductCustomer(productId,locale);
    };
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/viewAllProductsCustomer")
    ResponseEntity<List<ViewAllProductsCustomerDTO>> viewAllProductsCustomer(@RequestParam String categoryId,@RequestParam(required = false, defaultValue = "10") Integer pageSize, @RequestParam(required = false, defaultValue = "0") Integer pageOffset,
                                                                     @RequestParam(required = false, defaultValue = "id") String sortBy, @RequestParam(required = false,defaultValue = "desc") String order, @RequestParam(required = false) String query, Locale locale){
        return productService.viewAllProductsCustomer(categoryId,pageSize,pageOffset,sortBy,order,query,locale);
    };
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/viewSimilarProductsCustomer")
    ResponseEntity<List<ViewAllProductsCustomerDTO>> viewSimilarProductsCustomer(@RequestParam String productId,@RequestParam(required = false, defaultValue = "10") Integer pageSize, @RequestParam(required = false, defaultValue = "0") Integer pageOffset,
                                                                             @RequestParam(required = false, defaultValue = "id") String sortBy, @RequestParam(required = false,defaultValue = "desc") String order, @RequestParam(required = false) String query, Locale locale){
        return productService.viewSimilarProductsCustomer(productId,pageSize,pageOffset,sortBy,order,query,locale);
    };
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/viewProductAdmin")
    ResponseEntity<ViewProductAdminDTO> viewProductAdmin(String productId, Locale locale){
        return productService.viewProductAdmin(productId,locale);
    };
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/viewAllProductsAdmin")
    ResponseEntity<List<ViewProductAdminDTO>> viewAllProductsAdmin(@RequestParam(required = false, defaultValue = "10") Integer pageSize, @RequestParam(required = false, defaultValue = "0") Integer pageOffset,
                                                                     @RequestParam(required = false, defaultValue = "id") String sortBy, @RequestParam(required = false,defaultValue = "desc") String order, @RequestParam(required = false) String query, Locale locale){
        return productService.viewAllProductsAdmin(pageSize,pageOffset,sortBy,order,query,locale);
    };
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/deactivateProduct")
    ResponseEntity<String> deactivateProduct(@RequestParam String productId, Locale locale){
        return productService.deactivateProduct(productId,locale);
    };
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/activateProduct")
    ResponseEntity<String> activateProduct(@RequestParam String productId, Locale locale){
        return productService.activateProduct(productId,locale);
    };

}
