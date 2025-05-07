package com.bootcamp.ecommerce_rohit.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO {
    String id;
    String category_name;
    String parentCategory_id;
}
