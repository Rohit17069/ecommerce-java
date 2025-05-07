package com.bootcamp.ecommerce_rohit.repositories;

import com.bootcamp.ecommerce_rohit.entities.Category;
import com.bootcamp.ecommerce_rohit.entities.CategoryMetadataField;
import com.bootcamp.ecommerce_rohit.entities.CategoryMetadataFieldValuesId;
import com.bootcamp.ecommerce_rohit.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface CategoryMetadataFieldRepository extends JpaRepository<CategoryMetadataField,UUID> {
public CategoryMetadataField findByName(String fieldName);
    @Query("SELECT c FROM CategoryMetadataField c WHERE c.name LIKE %:fieldNameFilterValue%")
    public Page<CategoryMetadataField> findByName(@Param("fieldNameFilterValue") String fieldNameFilterValue, Pageable pageable);
    CategoryMetadataField findById(String id);

}
