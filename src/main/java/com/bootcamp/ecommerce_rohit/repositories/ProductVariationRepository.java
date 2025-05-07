package com.bootcamp.ecommerce_rohit.repositories;

import com.bootcamp.ecommerce_rohit.entities.ProductVariation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductVariationRepository extends JpaRepository<ProductVariation, UUID> {

    ProductVariation findById(String productVariationId);
    Page<ProductVariation> findAll(Specification<ProductVariation> specification, Pageable pageable);
    List<ProductVariation> findByProductId(String productId);

}

