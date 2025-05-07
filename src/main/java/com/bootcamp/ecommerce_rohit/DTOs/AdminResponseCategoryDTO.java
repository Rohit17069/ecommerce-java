package com.bootcamp.ecommerce_rohit.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminResponseCategoryDTO {
List<CategoryDTO> parentCategories;
String id;
String category_name;
String parentCategory_id;
List<MetadataFieldDTO> metadataFieldValues;
List<CategoryDTO> immediateChildrenCategories;
}
