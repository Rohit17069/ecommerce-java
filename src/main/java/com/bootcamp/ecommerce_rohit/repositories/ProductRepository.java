package com.bootcamp.ecommerce_rohit.repositories;

import com.bootcamp.ecommerce_rohit.entities.Category;
import com.bootcamp.ecommerce_rohit.entities.Product;
import com.bootcamp.ecommerce_rohit.entities.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Product findById(String id);
    Page<Product> findAll(Specification<Product> specification, Pageable pageable);
    Boolean existsByNameAndBrandAndCategoryAndSeller(String name, String brand, Category category, Seller seller);

}
