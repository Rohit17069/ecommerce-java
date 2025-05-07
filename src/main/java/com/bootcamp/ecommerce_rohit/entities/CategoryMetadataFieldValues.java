package com.bootcamp.ecommerce_rohit.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CategoryMetadataFieldValues extends AuditableEntity {
    @EmbeddedId
    private CategoryMetadataFieldValuesId id;
    @ManyToOne
    @MapsId("category")
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @MapsId("categoryMetadataField")
    @JoinColumn(name = "category_metadata_field_id")
    private CategoryMetadataField categoryMetadataField;

    private String categoryMetadataValues;

}
