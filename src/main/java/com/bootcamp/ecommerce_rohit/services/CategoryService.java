package com.bootcamp.ecommerce_rohit.services;

import com.bootcamp.ecommerce_rohit.DTOs.*;
import com.bootcamp.ecommerce_rohit.entities.*;
import com.bootcamp.ecommerce_rohit.exceptionsHandling.InvalidParametersException;
import com.bootcamp.ecommerce_rohit.exceptionsHandling.PaginationError;
import com.bootcamp.ecommerce_rohit.repositories.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    MessageSource messageSource;
    @Autowired
    public CategoryMetadataFieldRepository categoryMetadataFieldRepository;
    @Autowired
    CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductVariationRepository productVariationRepository;

    public ResponseEntity<String> addMetadataField(String fieldName, Locale locale) {
        if (categoryMetadataFieldRepository.findByName(fieldName) != null) {
            String message = messageSource.getMessage("category.fieldnameAlreadyExist", null, locale);

            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        ;
        CategoryMetadataField categoryMetadataField = new CategoryMetadataField(fieldName);
        categoryMetadataFieldRepository.save(categoryMetadataField);
        String message = messageSource.getMessage("category.fieldNameAdded", null, locale);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    public ResponseEntity<List<CategoryMetadataFieldDTO>> viewAllMetadataField(Integer pageSize,
              Integer pageOffset, String sortBy, String order, String query, Locale locale) {
        Page<CategoryMetadataField> page;
        List<CategoryMetadataField> categoryMetadataFields;
        Integer sizee = categoryMetadataFieldRepository.findAll().size();
        if (pageOffset < 0 || (double) pageOffset >= (double) sizee / (double) pageSize) {
            throw new PaginationError("invalid pageOffset");
        }
        try {
            Sort.Direction direction = order.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(pageOffset, pageSize, direction, sortBy);
            if (query == null) {
                page = categoryMetadataFieldRepository.findAll(pageable);
            } else {
                page = categoryMetadataFieldRepository.findByName(query, pageable);
            }

            categoryMetadataFields = page.getContent();
        } catch (Exception ex) {
            throw new InvalidParametersException(ex.getMessage());
        }
        List<CategoryMetadataFieldDTO> categoryMetadataFieldDTOList = new ArrayList<>();
        categoryMetadataFields.stream().forEach(field -> {
            CategoryMetadataFieldDTO categoryMetadataFieldDTO = new CategoryMetadataFieldDTO();
            categoryMetadataFieldDTO.setId(field.getId());
            categoryMetadataFieldDTO.setName(field.getName());
            categoryMetadataFieldDTOList.add(categoryMetadataFieldDTO);
        });
        return new ResponseEntity<>(categoryMetadataFieldDTOList, HttpStatus.OK);

    }

    public ResponseEntity<String> addCategory(String categoryName, String parentId, Locale locale) {
        if (categoryName.equals("")) {
            return new ResponseEntity<>("Category name can't be blank", HttpStatus.BAD_REQUEST);
        }
//        if(categoryRepository.findById(parentId)!=null){
//            return new ResponseEntity<>("parent is a leaf category,can't be a parent category anymore",HttpStatus.BAD_REQUEST);
//        }
        //checking all roots
        if (categoryRepository.findByNameAndParentCategoryIdIsNull(categoryName) != null) {
            return new ResponseEntity<>("category name is already in use!! try a different one", HttpStatus.BAD_REQUEST);
        }
        //if parent is null then create it as a root
        if (parentId == null) {
            Category category = new Category();
            category.setName(categoryName);
            categoryRepository.save(category);
            return new ResponseEntity<>("Category created successfully!!", HttpStatus.OK);
        }

        String duplicateParentId = parentId;
        Category parent = null;
        // checking for true siblings
        if (categoryRepository.findByNameAndParentCategoryId(categoryName, parentId) != null) {
            return new ResponseEntity<>("category name is already in use!! try a different one", HttpStatus.BAD_REQUEST);
        }
        List<CategoryMetadataFieldValues> categoryMetadataFieldValues = categoryMetadataFieldValuesRepository.findByCategoryId(parentId);
if(!categoryMetadataFieldValues.isEmpty()){
    return new ResponseEntity<>("parent is a leaf category,can't be a parent category anymore",HttpStatus.BAD_REQUEST);
}
//checking for depth
        while (duplicateParentId != null) {
            parent = categoryRepository.findById(duplicateParentId);
            if (parent.getName().equals(categoryName)) {
                return new ResponseEntity<>("category name is already in use!! try a different one", HttpStatus.BAD_REQUEST);
            }
            if (parent.getParentCategory() == null) {
                break;
            }
            parent = parent.getParentCategory();
            duplicateParentId = parent.getId();
        }


        Category category = new Category();
        category.setName(categoryName);
        category.setParentCategory(categoryRepository.findById(parentId));
        category.getParentCategory().setIsLeaf(false);
        categoryRepository.save(category);
        return new ResponseEntity<>("Category created successfully!!", HttpStatus.OK);
    }

    public AdminResponseCategoryDTO viewCategory(String categoryId, Locale locale) {
        AdminResponseCategoryDTO adminResponseCategoryDTO = new AdminResponseCategoryDTO();
        Category category = categoryRepository.findById(categoryId);

        if (category == null) {
            throw new InvalidParametersException("No category exists with this id!! try a different one");
        }

        adminResponseCategoryDTO.setCategory_name(category.getName());
        adminResponseCategoryDTO.setId(category.getId());

        if (category.getParentCategory() != null) {
            adminResponseCategoryDTO.setParentCategory_id(category.getParentCategory().getId());
        }

        List<CategoryDTO> parentCategoryList = new LinkedList<>();
        List<CategoryDTO> childCategoryList = new LinkedList<>();

        Category parent = category.getParentCategory();
        while (parent != null) {
            CategoryDTO parentDTO = new CategoryDTO();
            parentDTO.setCategory_name(parent.getName());
            parentDTO.setId(parent.getId());

            if (parent.getParentCategory() != null) {
                parentDTO.setParentCategory_id(parent.getParentCategory().getId());
            }

            parentCategoryList.addFirst(parentDTO);
            parent = parent.getParentCategory();
        }
        adminResponseCategoryDTO.setParentCategories(parentCategoryList);

        List<Category> children = categoryRepository.findByParentCategoryId(category.getId());
        for (Category child : children) {
            CategoryDTO childDTO = new CategoryDTO();
            childDTO.setId(child.getId());
            childDTO.setCategory_name(child.getName());

            if (child.getParentCategory() != null) {
                childDTO.setParentCategory_id(child.getParentCategory().getId());
            }

            childCategoryList.add(childDTO);
        }
        adminResponseCategoryDTO.setImmediateChildrenCategories(childCategoryList);
List<MetadataFieldDTO> metadataFieldDTOList = new ArrayList<>();
List<CategoryMetadataFieldValues> rawMetadataFields=new ArrayList<>();
        rawMetadataFields=categoryMetadataFieldValuesRepository.findByCategoryId(categoryId);
        rawMetadataFields.stream().forEach(categoryMetadataFieldValues -> {
            MetadataFieldDTO metadataFieldDTO = new MetadataFieldDTO();
            metadataFieldDTO.setFiledId(categoryMetadataFieldValues.getCategoryMetadataField().getId());
            metadataFieldDTO.setFieldName(categoryMetadataFieldValues.getCategoryMetadataField().getName());
            metadataFieldDTO.setValues(categoryMetadataFieldValues.getCategoryMetadataValues());
            metadataFieldDTOList.add(metadataFieldDTO);
        });
        adminResponseCategoryDTO.setMetadataFieldValues(metadataFieldDTOList);
        return adminResponseCategoryDTO;
    }


    public ResponseEntity<List<AdminResponseCategoryDTO>> viewAllCategoriesForAdmin(Integer pageSize,
                                                                                    Integer pageOffset, String sortBy, String order, String query, Locale locale) {
        List<Category> categoryList = new ArrayList<>();
        Page<Category> page;
        List<AdminResponseCategoryDTO> adminResponseCategoryDTOList = new ArrayList<>();

        Integer sizee = categoryRepository.findAll().size();
        if (pageOffset < 0 || (double) pageOffset >= (double) sizee / (double) pageSize) {
            throw new PaginationError("invalid pageOffset");
        }
        try {
            Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(pageOffset, pageSize, direction, sortBy);
            if (query == null) {
                page = categoryRepository.findAll(pageable);
            } else {
                page = categoryRepository.findByName(query, pageable);
            }

            categoryList = page.getContent();
        } catch (Exception ex) {
            throw new InvalidParametersException(ex.getMessage());
        }
        categoryList.stream().forEach(category -> {
            AdminResponseCategoryDTO adminResponseCategoryDTO = new AdminResponseCategoryDTO();
            adminResponseCategoryDTO = viewCategory(category.getId(), locale);
            adminResponseCategoryDTOList.add(adminResponseCategoryDTO);
        });
        return new ResponseEntity<>(adminResponseCategoryDTOList, HttpStatus.OK);
    }

    public ResponseEntity<String> updateCategory(String categoryId, String newName, Locale locale) {
        if (categoryId.equals("")) {
            return new ResponseEntity<>("Category Id can't be  blank", HttpStatus.BAD_REQUEST);
        }
        //checking all roots
        if (categoryRepository.findByNameAndParentCategoryIdIsNull(newName) != null) {
            return new ResponseEntity<>("category name is already in use!! try a different one", HttpStatus.BAD_REQUEST);
        }
        Category category=categoryRepository.findById(categoryId);
        String parentId=category.getParentCategory().getId();
        //if parent is null then create it as a root
        if ( parentId== null) {
            category.setName(newName);
            categoryRepository.save(category);
            return new ResponseEntity<>("Category created successfully!!", HttpStatus.OK);
        }

        String duplicateParentId = parentId;
        Category parent = null;
        // checking for true siblings
        if (categoryRepository.findByNameAndParentCategoryId(newName, parentId) != null) {
            return new ResponseEntity<>("category name is already in use!! try a different one", HttpStatus.BAD_REQUEST);
        }
//checking for depth
        while (duplicateParentId != null) {
            parent = categoryRepository.findById(duplicateParentId);
            if (parent.getName().equals(newName)) {
                return new ResponseEntity<>("category name is already in use!! try a different one", HttpStatus.BAD_REQUEST);
            }
            if (parent.getParentCategory() == null) {
                break;
            }
            parent = parent.getParentCategory();
            duplicateParentId = parent.getId();
        }


//        List<Category> children = categoryRepository.findAllByParentCategoryId(categoryId);
//        for (Category child : children) {
//            if (child.getName().equalsIgnoreCase(newName)) {
//                return new ResponseEntity<>("Category name already exists as a child of this category!", HttpStatus.BAD_REQUEST);
//            }
//        }
        category.setName(newName);
        categoryRepository.save(category);
        return new ResponseEntity<>("Category updated successfully!", HttpStatus.OK);
    }

    public ResponseEntity<String> addCategoryMetadataFieldValues(@Valid CategoryMetadataFieldValuesDTO categoryMetadataFieldValuesDTO, Locale locale) {

        if (categoryRepository.findById(categoryMetadataFieldValuesDTO.getCategoryId()) == null || categoryMetadataFieldRepository.findById(categoryMetadataFieldValuesDTO.getMetadataFieldId()) == null) {
            return new ResponseEntity<>("Category or Category Metadata field does not exist!!", HttpStatus.BAD_REQUEST);
        }

        if (!categoryRepository.findById(categoryMetadataFieldValuesDTO.getCategoryId()).getIsLeaf()) {
            return new ResponseEntity<>("this category isn't a leaf category!can't add values", HttpStatus.BAD_REQUEST);
        }
        ;

        CategoryMetadataFieldValuesId id = new CategoryMetadataFieldValuesId(categoryMetadataFieldValuesDTO.getCategoryId(), categoryMetadataFieldValuesDTO.getMetadataFieldId());
        if (categoryMetadataFieldValuesRepository.findById(id).isPresent()) {
            return new ResponseEntity<>("Values already present!!", HttpStatus.BAD_REQUEST);
        }
        ;
        String values = categoryMetadataFieldValuesDTO.getValues();
        if (!areValuesUnique(values)) {
            return new ResponseEntity<>("values can't repeat or be null !!,request failed", HttpStatus.BAD_REQUEST);
        }
        values = Arrays.stream(values.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.joining(","));
        CategoryMetadataFieldValues categoryMetadataFieldValues = new CategoryMetadataFieldValues();
        categoryMetadataFieldValues.setId(id);
        categoryMetadataFieldValues.setCategoryMetadataField(categoryMetadataFieldRepository.findById(categoryMetadataFieldValuesDTO.getMetadataFieldId()));
        categoryMetadataFieldValues.setCategory(categoryRepository.findById(categoryMetadataFieldValuesDTO.getCategoryId()));
        categoryMetadataFieldValues.setCategoryMetadataValues(values);
        categoryMetadataFieldValuesRepository.save(categoryMetadataFieldValues);
        Category category = categoryRepository.findById(categoryMetadataFieldValuesDTO.getCategoryId());
        categoryRepository.save(category);
        return new ResponseEntity<>("Category Metadata field with values added successfully!", HttpStatus.OK);

    }

    public ResponseEntity<String> updateCategoryMetadataFieldValues(@Valid CategoryMetadataFieldValuesDTO categoryMetadataFieldValuesDTO, Locale locale) {
        if (categoryRepository.findById(categoryMetadataFieldValuesDTO.getCategoryId()) == null || categoryMetadataFieldRepository.findById(categoryMetadataFieldValuesDTO.getMetadataFieldId()) == null) {
            return new ResponseEntity<>("Category or Category Metadata field does not exist!!", HttpStatus.BAD_REQUEST);
        }

        CategoryMetadataFieldValuesId id = new CategoryMetadataFieldValuesId(categoryMetadataFieldValuesDTO.getCategoryId(), categoryMetadataFieldValuesDTO.getMetadataFieldId());
        if (!categoryMetadataFieldValuesRepository.findById(id).isPresent()) {
            return new ResponseEntity<>("Values dosen't exists can't update!!", HttpStatus.BAD_REQUEST);
        }
        ;
        String combinedValues = categoryMetadataFieldValuesRepository.findById(id).get().getCategoryMetadataValues() + "," + categoryMetadataFieldValuesDTO.getValues();
        if (!areValuesUnique(combinedValues)) {
            return new ResponseEntity<>("values can't repeat or be null !!,request failed", HttpStatus.BAD_REQUEST);
        }
        combinedValues = Arrays.stream(combinedValues.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.joining(","));

        categoryMetadataFieldValuesRepository.findById(id).get().setCategoryMetadataValues(combinedValues);
        categoryMetadataFieldValuesRepository.save(categoryMetadataFieldValuesRepository.findById(id).get());
        return new ResponseEntity<>("Category Metadata field with values updated successfully!", HttpStatus.OK);

    }

    public boolean areValuesUnique(String input) {
        String[] values = input.split(",");
        Set<String> seen = new HashSet<>();

        for (String val : values) {
            val = val.trim();
            val = val.toLowerCase();
            if (val.equals("")) {
                return false;
            }
            if (seen.contains(val)) return false;
            seen.add(val);
        }
        return true;
    }

    public ResponseEntity<List<SellerResponseCategoryDTO>> viewAllCategoriesForSeller(Locale locale) {
        List<Category> categories = categoryRepository.findAllByIsLeafTrue();
        List<SellerResponseCategoryDTO> sellerResponseCategoryDTOList = new ArrayList<>();

        categories.forEach(category -> {
            SellerResponseCategoryDTO dto = new SellerResponseCategoryDTO();
            dto.setCategory_name(category.getName());
            dto.setId(category.getId());

            if (category.getParentCategory() != null) {
                dto.setParentCategory_id(category.getParentCategory().getId());
            }

            List<CategoryDTO> parentCategoryList = new LinkedList<>();
            Category parent = category.getParentCategory();
            while (parent != null) {
                CategoryDTO parentDTO = new CategoryDTO();
                parentDTO.setCategory_name(parent.getName());
                parentDTO.setId(parent.getId());

                if (parent.getParentCategory() != null) {
                    parentDTO.setParentCategory_id(parent.getParentCategory().getId());
                }

                parentCategoryList.addFirst(parentDTO);
                parent = parent.getParentCategory();
            }

            List<MetadataFieldDTO> metadataFieldDTOList = new ArrayList<>();
            List<CategoryMetadataFieldValues> rawMetadataFields=new ArrayList<>();
            rawMetadataFields=categoryMetadataFieldValuesRepository.findByCategoryId(category.getId());
            rawMetadataFields.stream().forEach(categoryMetadataFieldValues -> {
                MetadataFieldDTO metadataFieldDTO = new MetadataFieldDTO();
                metadataFieldDTO.setFiledId(categoryMetadataFieldValues.getCategoryMetadataField().getId());
                metadataFieldDTO.setFieldName(categoryMetadataFieldValues.getCategoryMetadataField().getName());
                metadataFieldDTO.setValues(categoryMetadataFieldValues.getCategoryMetadataValues());
                metadataFieldDTOList.add(metadataFieldDTO);
            });
            dto.setMetadataFieldValues(metadataFieldDTOList);
            dto.setParentCategories(parentCategoryList);
            sellerResponseCategoryDTOList.add(dto);
        });

        return new ResponseEntity<>(sellerResponseCategoryDTOList, HttpStatus.OK);
    }

    public ResponseEntity<List<CategoryDTO>> viewAllCategoriesForCustomer(String categoryId, Locale locale) {
        List<CategoryDTO> categoryDTOList = new ArrayList<>();
        List<Category> categoryList;

        if (categoryId != null) {
            if (categoryRepository.findById(categoryId) == null) {
                throw new InvalidParametersException("id is blank or No category exists with this id");
            }
            if (categoryRepository.findById(categoryId).getIsLeaf()) {
                throw new InvalidParametersException("It's a leaf category,please pass a parent category id.");
            }
            categoryList = categoryRepository.findAllByParentCategoryId(categoryId);
        } else {
            categoryList = categoryRepository.findAllByParentCategoryIsNull();
        }

        categoryList.forEach(category -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setCategory_name(category.getName());
            categoryDTO.setId(category.getId());

            if (category.getParentCategory() != null) {
                categoryDTO.setParentCategory_id(category.getParentCategory().getId());
            }

            categoryDTOList.add(categoryDTO);
        });

        return new ResponseEntity<>(categoryDTOList, HttpStatus.OK);
    }

    public ResponseEntity<CategoryFiltersDTO> getFilteringDetails(String categoryId, Locale locale) {
        CategoryFiltersDTO categoryFiltersDTO = new CategoryFiltersDTO();
        if (categoryRepository.findById(categoryId) == null) {
            throw new InvalidParametersException("id is blank or No category exists with this id");
        }
        Category root = categoryRepository.findById(categoryId);
        List<String> categoryIds = getAllSubCategoryId2s(root);
        List<Category> categories = categoryIds.stream()
                .map(categoryRepository::findById)
                .collect(Collectors.toList());
        Set<MetadataFieldDTO> metadataFeilds = new HashSet<>();
        Set<String> brands = new HashSet<>();
        AtomicReference<Integer> minPrice = new AtomicReference<>(Integer.MAX_VALUE);
        AtomicReference<Integer> maxPrice = new AtomicReference<>(0);
        categories.stream().forEach(category -> {
            category.getMetadataFieldValues().stream().forEach(categoryMetadataFieldValues -> {


                CategoryMetadataField categoryMetadataField = categoryMetadataFieldRepository.findById(categoryMetadataFieldValues.getCategoryMetadataField().getId());
                if (categoryMetadataField != null) {
                    MetadataFieldDTO metadataFieldDTO = new MetadataFieldDTO();
                    metadataFieldDTO.setFiledId(categoryMetadataField.getId());
                    metadataFieldDTO.setFieldName(categoryMetadataField.getName());
                    String values = categoryMetadataFieldValues.getCategoryMetadataValues();
                    metadataFieldDTO.setValues(values);
                    metadataFeilds.add(metadataFieldDTO);
                }
                ;
                category.getProducts().stream().forEach(product -> {
                    if (product.getBrand() != null) {
                        brands.add(product.getBrand());
                    }
                    ;
                    product.getProductVariations().stream().forEach(productVariation -> {
                        if (productVariation.getPrice() != null) {
                            if (productVariation.getPrice() < minPrice.get()) {
                                minPrice.set(productVariation.getPrice());
                            }
                            if (productVariation.getPrice() > maxPrice.get()) {
                                maxPrice.set(productVariation.getPrice());
                            }
                        }
                    });

                });
            });

        });
        categoryFiltersDTO.setBrands(brands);
        categoryFiltersDTO.setMetadataFields(metadataFeilds);
        categoryFiltersDTO.setMinPrice(minPrice.get());
        categoryFiltersDTO.setMaxPrice(maxPrice.get());

        return new ResponseEntity<>(categoryFiltersDTO, HttpStatus.OK);
    }

    public List<String> getAllSubCategoryId2s(Category category) {
        List<String> leafCategoryIds = new ArrayList<>();
        Queue<Category> queue = new LinkedList<>();
        queue.add(category);
        while (!queue.isEmpty()) {
            Category currentCategory = queue.poll();

            if (currentCategory.getIsLeaf()) {
                leafCategoryIds.add(currentCategory.getId());
            } else {

                queue.addAll(categoryRepository.findAllByParentCategoryId(currentCategory.getId()));
            }
        }
        return leafCategoryIds;
    }
    }
