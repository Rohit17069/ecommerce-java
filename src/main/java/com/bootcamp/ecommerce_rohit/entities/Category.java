package com.bootcamp.ecommerce_rohit.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Category extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
   @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_category_Id")
    private Category parentCategory;

   @OneToMany(mappedBy = "category")
    private List<Product>products=new LinkedList<>();
private Boolean isLeaf=true;
    public Category(String name, Category parentCategory, List<Product> products) {
        this.name = name;
        this.parentCategory = parentCategory;
        this.products = products;
    }

    @OneToMany(mappedBy = "category")
    private List<CategoryMetadataFieldValues> metadataFieldValues;
}
