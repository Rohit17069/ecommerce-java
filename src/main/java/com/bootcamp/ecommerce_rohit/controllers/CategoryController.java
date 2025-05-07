package com.bootcamp.ecommerce_rohit.controllers;

import com.bootcamp.ecommerce_rohit.DTOs.*;
import com.bootcamp.ecommerce_rohit.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
public class CategoryController {
@Autowired
    CategoryService categoryService;


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/addMetadataField")
    ResponseEntity<String> addMetadataField(@RequestParam String fieldName, Locale locale){
        return categoryService.addMetadataField(fieldName,locale);
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/viewAllMetadataField")
    ResponseEntity<List<CategoryMetadataFieldDTO>> viewAllMetadataField(@RequestParam(required = false, defaultValue = "10") Integer pageSize, @RequestParam(required = false,defaultValue = "0")
            Integer pageOffset, @RequestParam(required = false,defaultValue = "id") String sortBy, @RequestParam(required = false,defaultValue = "asc") String order, @RequestParam(required = false) String query, Locale locale){
        return categoryService.viewAllMetadataField(pageSize,pageOffset,sortBy,order,query,locale);
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/addCategory")
    ResponseEntity<String> addCategory(@RequestParam String  categoryName,@RequestParam(required = false)
    String parentId,  Locale locale){
        return categoryService.addCategory(categoryName,parentId,locale);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/viewCategory")
    ResponseEntity<AdminResponseCategoryDTO> viewCategory(@RequestParam String  categoryId , Locale locale){
        AdminResponseCategoryDTO adminResponseCategoryDTO =categoryService.viewCategory(categoryId,locale);
        return new ResponseEntity<>(adminResponseCategoryDTO,HttpStatus.OK);
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/viewAllCategoriesForAdmin")
    ResponseEntity<List<AdminResponseCategoryDTO>> viewAllCategoriesForAdmin(@RequestParam(required = false, defaultValue = "10") Integer pageSize, @RequestParam(required = false,defaultValue = "0")
    Integer pageOffset, @RequestParam(required = false,defaultValue = "id") String sortBy, @RequestParam(required = false,defaultValue = "asc") String order, @RequestParam(required = false) String query, Locale locale){
        return categoryService.viewAllCategoriesForAdmin(pageSize,pageOffset,sortBy,order,query,locale);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/updateCategory")
    ResponseEntity<String> updateCategory(@RequestParam String categoryId, @RequestParam  String newName, Locale locale){
        return categoryService.updateCategory(categoryId,newName,locale);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/addCategoryMetadataFieldValues")
    ResponseEntity<String> addCategoryMetadataFieldValues(@Valid @RequestBody CategoryMetadataFieldValuesDTO categoryMetadataFieldValuesDTO, Locale locale){
        return categoryService.addCategoryMetadataFieldValues(categoryMetadataFieldValuesDTO,locale);
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/updateCategoryMetadataFieldValues")
    ResponseEntity<String> updateCategoryMetadataFieldValues(@Valid @RequestBody CategoryMetadataFieldValuesDTO categoryMetadataFieldValuesDTO, Locale locale){
        return categoryService.updateCategoryMetadataFieldValues(categoryMetadataFieldValuesDTO,locale);
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/viewAllCategoriesForSeller")
    ResponseEntity<List<SellerResponseCategoryDTO>> viewAllCategoriesForSeller(Locale locale){
        return categoryService.viewAllCategoriesForSeller(locale);
    }
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/viewAllCategoriesForCustomer")
    ResponseEntity<List<CategoryDTO>> viewAllCategoriesForCustomer(@RequestParam(required = false)String categoryId,Locale locale){
        return categoryService.viewAllCategoriesForCustomer(categoryId, locale);
    }
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/getFilteringDetails")
    ResponseEntity<CategoryFiltersDTO> getFilteringDetails(@RequestParam String categoryId,Locale locale){
        return categoryService.getFilteringDetails(categoryId, locale);
    }

}
