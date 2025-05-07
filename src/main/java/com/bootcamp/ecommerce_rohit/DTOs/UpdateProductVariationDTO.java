package com.bootcamp.ecommerce_rohit.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UpdateProductVariationDTO {
    @NotBlank(message = "product variation Id can't be null or blank.")
    private String productVariationId;
    private Boolean isActive;
    private Integer quantityAvailable;
    private Integer price;
    private String metadata;
    private MultipartFile primaryImage;
    private List<MultipartFile> secondaryImages;

}
