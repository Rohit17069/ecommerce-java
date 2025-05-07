package com.bootcamp.ecommerce_rohit.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "seller_user_id")
    private Seller seller;
    private String name;
    private String description;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private Category category;
    private Boolean isCancellable=false;
    private Boolean isReturnable=false;
    private String brand;
    private Boolean isActive=false;
    private Boolean isDeleted=false;

    @OneToMany(mappedBy = "product")
    private List<ProductVariation>productVariations=new ArrayList<>();

    @OneToMany(mappedBy = "product")
private List<ProductReview>productReviews=new ArrayList<>();

    public Product(Seller seller, String name, String description, Category category, Boolean isCancellable, Boolean isReturnable, String brand, Boolean isActive, Boolean isDeleted, List<ProductVariation> productVariations, List<ProductReview> productReviews) {
        this.seller = seller;
        this.name = name;
        this.description = description;
        this.category = category;
        this.isCancellable = isCancellable;
        this.isReturnable = isReturnable;
        this.brand = brand;
        this.isActive = isActive;
        this.isDeleted = isDeleted;
        this.productVariations = productVariations;
        this.productReviews = productReviews;
    }
}