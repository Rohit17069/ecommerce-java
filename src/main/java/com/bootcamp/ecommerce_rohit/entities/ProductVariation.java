package com.bootcamp.ecommerce_rohit.entities;


import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ProductVariation extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(cascade = CascadeType.ALL )
    @JoinColumn(name = "product_id")
    private Product product;
    private Integer quantityAvailable;
    private Integer price;
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
private Map<String ,Object>metadata;
    private String primaryImageName;
    private Boolean isActive=true;

    public ProductVariation(Product product, Integer quantityAvailable, Integer price, Map<String, Object> metadata, String primaryImageName, Boolean isActive) {
        this.product = product;
        this.quantityAvailable = quantityAvailable;
        this.price = price;
        this.metadata = metadata;
        this.primaryImageName = primaryImageName;
        this.isActive = isActive;
    }
}

