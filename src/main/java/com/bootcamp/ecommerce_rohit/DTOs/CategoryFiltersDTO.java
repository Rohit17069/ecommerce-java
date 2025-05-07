package com.bootcamp.ecommerce_rohit.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class CategoryFiltersDTO {
    private Set<MetadataFieldDTO> metadataFields;
    private Set<String> brands;
    private Integer minPrice;
    private Integer maxPrice;
}
