package com.bootcamp.ecommerce_rohit.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Setter
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CategoryMetadataFieldValuesId implements Serializable{
    String category;
    String categoryMetadataField;


}
