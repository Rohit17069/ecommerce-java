package com.bootcamp.ecommerce_rohit.DTOs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ViewAllProductsCustomerDTO {
    private String id;
    private String sellerId;
    private String name;
    private String description;
    private String categoryName;
    private String categoryId;
    private String parentCategoryId;
    private Boolean isCancellable;
    private Boolean isReturnable;
    private String brand;
    private Boolean isActive;
    private String companyName;
    private List<String>productImages;
}
