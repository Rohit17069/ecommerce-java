package com.bootcamp.ecommerce_rohit.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CategoryMetadataField extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;

    public CategoryMetadataField(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "categoryMetadataField")
    private List<CategoryMetadataFieldValues> fieldValues;
}
