package com.bootcamp.ecommerce_rohit.repositories;

import com.bootcamp.ecommerce_rohit.entities.Category;
import com.bootcamp.ecommerce_rohit.entities.CategoryMetadataField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
  Category findByNameAndParentCategoryIdIsNull(String categoryName);
  Category findById(String categoryId);
  Category findByNameAndParentCategoryId(String categoryName, String parentId);
  List<Category> findByParentCategoryId(String parentId);

  Category findByName(String name);
  @Query("SELECT c FROM Category c WHERE c.name LIKE %:fieldNameFilterValue%")
   Page<Category> findByName(@Param("fieldNameFilterValue") String nameFilterValue, Pageable pageable);

  List<Category> findAllByParentCategoryId(String parentId);
  List<Category> findAllByIsLeafTrue();
  List<Category> findAllByParentCategoryIsNull();

}
