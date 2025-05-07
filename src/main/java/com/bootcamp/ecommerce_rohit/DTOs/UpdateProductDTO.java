package com.bootcamp.ecommerce_rohit.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductDTO {
    @NotBlank(message = "Product ID is can't be null or blank")
    String productId;
    String name;
    String description;
    Boolean isCancellable;
    Boolean isReturnable;
}
