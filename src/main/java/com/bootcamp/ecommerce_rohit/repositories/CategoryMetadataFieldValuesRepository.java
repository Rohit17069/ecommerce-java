package com.bootcamp.ecommerce_rohit.repositories;

import com.bootcamp.ecommerce_rohit.entities.CategoryMetadataFieldValues;
import com.bootcamp.ecommerce_rohit.entities.CategoryMetadataFieldValuesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface CategoryMetadataFieldValuesRepository extends JpaRepository<CategoryMetadataFieldValues, CategoryMetadataFieldValuesId> {
    CategoryMetadataFieldValues findByCategory_IdAndCategoryMetadataField_Id(String categoryId, String  categoryMetadataFieldId);

    List<CategoryMetadataFieldValues> findByCategoryId(String parentId);

//CategoryMetadataFieldValues findById(String  id);
}
