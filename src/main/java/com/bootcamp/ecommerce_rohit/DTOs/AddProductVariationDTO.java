package com.bootcamp.ecommerce_rohit.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AddProductVariationDTO {
@NotBlank(message = "product id can't be null or blank")
String productId;
    @NotBlank(message = "metadata can't be null or blank")
    String metadata;
    @NotNull(message = "primary image can't be null")
    MultipartFile primaryImage;
    @NotNull(message = "quantity available can't be null")
    @PositiveOrZero(message = "quantity must be 0 or more")
    Integer quantityAvailable;
    @NotNull(message = "price can't be null")
    @PositiveOrZero(message = "price must be 0 or more")
    Integer price;
    List<MultipartFile> secondaryImagesList;
}
