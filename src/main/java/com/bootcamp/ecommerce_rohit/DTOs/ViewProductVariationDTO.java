package com.bootcamp.ecommerce_rohit.DTOs;

import com.bootcamp.ecommerce_rohit.entities.Product;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Map;
@Getter
@Setter
public class ViewProductVariationDTO {
    private String id;
    private Integer quantityAvailable;
    private Integer price;
    private Map<String ,Object> metadata;
    private String primaryImageName;
    private List<String> secondaryImageNames;
    private Boolean variationIsActive;
    private String productId;
    private String sellerId;
    private String productName;
    private String productDescription;
    private String CategoryName;
    private String CategoryId;
    private String parentCategoryId;
    private String brand;

}
