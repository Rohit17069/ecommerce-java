package com.bootcamp.ecommerce_rohit.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SellerResponseCategoryDTO {
    List<CategoryDTO> parentCategories;
    String id;
    String category_name;
    List<MetadataFieldDTO> metadataFieldValues;
    String parentCategory_id;
}
