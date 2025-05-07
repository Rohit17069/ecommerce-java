package com.bootcamp.ecommerce_rohit.DTOs;

import com.bootcamp.ecommerce_rohit.entities.CategoryMetadataField;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

@Getter
@Setter
public class  CategoryMetadataFieldDTO {
    private String id;
    private String name;
}
