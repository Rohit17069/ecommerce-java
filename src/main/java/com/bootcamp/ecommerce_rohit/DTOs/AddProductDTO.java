package com.bootcamp.ecommerce_rohit.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class AddProductDTO {

    @NotBlank(message = "Name of the product cannot be blank")
    private String name;
    @NotBlank(message = "Name of the brand cannot be null")
    private String brand;
    @NotBlank(message = "Please mention the category")
    private String categoryId;

    private String description;
    private Boolean isCancellable=false;
    private Boolean isReturnable=false;
}