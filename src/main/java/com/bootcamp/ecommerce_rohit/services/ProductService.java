package com.bootcamp.ecommerce_rohit.services;

import com.bootcamp.ecommerce_rohit.DTOs.*;
import com.bootcamp.ecommerce_rohit.entities.*;
import com.bootcamp.ecommerce_rohit.exceptionsHandling.InvalidParametersException;
import com.bootcamp.ecommerce_rohit.exceptionsHandling.PaginationError;
import com.bootcamp.ecommerce_rohit.repositories.*;
import com.bootcamp.ecommerce_rohit.specifications.ProductSpecifications;
import com.bootcamp.ecommerce_rohit.specifications.ProductVariationSpecifications;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ProductService {
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    SellerRepository sellerRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    MessageSource messageSource;
    @Autowired
    ImageUtils imageUtils;
    @Autowired
    ProductVariationRepository productVariationRepository;
    @Autowired
    CategoryMetadataFieldRepository categoryMetadataFieldRepository;
    @Autowired
    CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;

    public ResponseEntity<String> addProduct(@Valid AddProductDTO addProductDTO, Principal principal, Locale locale) {
        Seller seller = sellerRepository.findByEmail(principal.getName());
        Category category = categoryRepository.findById(addProductDTO.getCategoryId());
if(category==null){
    return new ResponseEntity<>("category not found",HttpStatus.NOT_FOUND);
}
if(!category.getIsLeaf()){
    return new ResponseEntity<>("Category isn't a leaf category,can't add product",HttpStatus.BAD_REQUEST);
}
        // Uniqueness check
        boolean exists = productRepository.existsByNameAndBrandAndCategoryAndSeller(
                addProductDTO.getName(), addProductDTO.getBrand(), category, seller);

        if (exists) {
            return new ResponseEntity<>("product should be a unique combination of name,brand,category and seller",HttpStatus.BAD_REQUEST);
        }
        Product product = new Product();
        product.setBrand(addProductDTO.getBrand());
        product.setCategory(categoryRepository.findById(addProductDTO.getCategoryId()));
        product.setName(addProductDTO.getName());
        if (addProductDTO.getDescription() != null) {
            product.setDescription(addProductDTO.getDescription());
        }
        if (addProductDTO.getIsCancellable() != null) {
            product.setIsCancellable(addProductDTO.getIsCancellable());
        }
        if (addProductDTO.getIsReturnable() != null) {
            product.setIsReturnable(addProductDTO.getIsReturnable());
        }
        product.setSeller(sellerRepository.findByEmail(principal.getName()));
        productRepository.save(product);
        ;
        try {

            emailService.sendEmail("rohit.gupta11@tothenew.com", "Verify this add product request by: " + principal.getName(),
                    product.toString());

        } catch (
                MessagingException e) {
            String message = messageSource.getMessage("email.error", null, locale);
            return new ResponseEntity<>(message + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("product added successfully,Approval by admin pending!!", HttpStatus.OK);
    }

    public ResponseEntity<String> addProductVariation(@Valid AddProductVariationDTO addProductVariationDTO, Principal principal, Locale locale) {
        Product product = productRepository.findById(addProductVariationDTO.getProductId());
        if (product == null) {
            return new ResponseEntity<>("product not found", HttpStatus.NOT_FOUND);
        }
        ;
        if (!product.getIsActive() || product.getIsDeleted()) {
            return new ResponseEntity<>("product should be active and non deleted.", HttpStatus.NOT_FOUND);
        }
        if (sellerRepository.findByEmail(principal.getName()) == null) {
            return new ResponseEntity<>("seller not found", HttpStatus.NOT_FOUND);
        }
        if (!product.getSeller().getId().equals(sellerRepository.findByEmail(principal.getName()).getId())) {
            return new ResponseEntity<>(" This product is not added by  this seller.", HttpStatus.NOT_FOUND);
        }
        ;
        ProductVariation productVariation = new ProductVariation();
        productVariation.setProduct(product);
        productVariation.setPrice(addProductVariationDTO.getPrice());
        Map<String, Object> metadata;
        try {
            metadata = new ObjectMapper().readValue(addProductVariationDTO.getMetadata(), new TypeReference<>() {
            });
            productVariation.setMetadata(metadata);
        } catch (IOException e) {
            throw new InvalidParametersException("Invalid metadata JSON format.");

        }
        AtomicBoolean f = new AtomicBoolean(false);
        metadata.forEach((key, value) -> {
            if (categoryMetadataFieldRepository.findByName(key) == null) {
                throw new InvalidParametersException("Invalid metadata field: " + key);
            }
            ;
            CategoryMetadataFieldValues categoryMetadataFieldValues = categoryMetadataFieldValuesRepository.findByCategory_IdAndCategoryMetadataField_Id(product.getCategory().getId(), categoryMetadataFieldRepository.findByName(key).getId());
            if (categoryMetadataFieldValues == null) {
                throw new InvalidParametersException("no metadata field values exists for this metadatafield: " + key);
            }
            ;
            if (!categoryMetadataFieldValues.getCategoryMetadataValues().contains(((String) value))) {
                throw new InvalidParametersException("Invalid metadata field value for field: " + key);

            }
            f.set(true);

        });
        if (f.get() == false) {
            return new ResponseEntity<>("at least one valid metadata field with valid value should be present", HttpStatus.NOT_FOUND);
        }
        ProductVariation savedProductVariation=productVariationRepository.save(productVariation);

        String imageId="primary_" + savedProductVariation.getId();
        if (addProductVariationDTO.getPrimaryImage() != null) {
            imageUtils.saveImageOnServer(addProductVariationDTO.getPrimaryImage(),imageId , "products");
        }
        productVariation.setQuantityAvailable(addProductVariationDTO.getQuantityAvailable());
        if (addProductVariationDTO.getSecondaryImagesList() != null) {
            for (MultipartFile secondaryImage : addProductVariationDTO.getSecondaryImagesList()) {
                imageUtils.saveImageOnServer(secondaryImage, savedProductVariation.getId() + "_" + (int) (Math.random() * 1000), "products");
            }
        }
        productVariation.setPrimaryImageName(imageId);
        productVariationRepository.save(productVariation);
        return new ResponseEntity<>("Product variation added successfully", HttpStatus.OK);
    }

    public ResponseEntity<ViewProductSellerDTO> viewProductSeller(String productId, Principal principal, Locale locale) {
        Seller seller = sellerRepository.findByEmail(principal.getName());


        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new InvalidParametersException("product not found");
        }
        ;
        if (product.getIsDeleted()) {
            throw new InvalidParametersException("product should not be deleted.");
        }
        if (!seller.getId().equals(product.getSeller().getId())) {
            throw new InvalidParametersException(" This product is not added by  this seller.");
        }
        ViewProductSellerDTO viewProductSellerDTO = new ViewProductSellerDTO();
        viewProductSellerDTO.setBrand(product.getBrand());
        viewProductSellerDTO.setCategoryId(product.getCategory().getId());
        viewProductSellerDTO.setName(product.getName());
        viewProductSellerDTO.setDescription(product.getDescription());
        viewProductSellerDTO.setIsCancellable(product.getIsCancellable());
        viewProductSellerDTO.setIsReturnable(product.getIsReturnable());
        viewProductSellerDTO.setSellerId(product.getSeller().getId());
        viewProductSellerDTO.setId(product.getId());
        viewProductSellerDTO.setIsActive(product.getIsActive());
        viewProductSellerDTO.setIsDeleted(product.getIsDeleted());
        viewProductSellerDTO.setParentCategoryId(product.getCategory().getParentCategory().getId());
        viewProductSellerDTO.setCategoryName(product.getCategory().getName());

        return new ResponseEntity<>(viewProductSellerDTO, HttpStatus.OK);
    }

    public ResponseEntity<ViewProductVariationDTO> viewProductVariation(String productVariationId, Principal principal, Locale locale) {
        Seller seller = sellerRepository.findByEmail(principal.getName());
        ProductVariation productVariation = productVariationRepository.findById(productVariationId);
        if (productVariation == null) {
            throw new InvalidParametersException("product variation not found");
        }
        ;
        Product product = productRepository.findById(productVariation.getProduct().getId());

        if (product.getIsDeleted()) {
            throw new InvalidParametersException("product should not be deleted.");
        }
        if (!seller.getId().equals(product.getSeller().getId())) {
            throw new InvalidParametersException(" This product is not added by  this seller.");
        }
        ViewProductVariationDTO viewProductVariationDTO = new ViewProductVariationDTO();
        viewProductVariationDTO.setPrice(productVariation.getPrice());
        viewProductVariationDTO.setQuantityAvailable(productVariation.getQuantityAvailable());
        viewProductVariationDTO.setMetadata(productVariation.getMetadata());
        viewProductVariationDTO.setProductId(product.getId());
        viewProductVariationDTO.setId(productVariation.getId());
        viewProductVariationDTO.setPrimaryImageName(imageUtils.getImageURL("primary_" + productVariationId, "products"));
        viewProductVariationDTO.setSecondaryImageNames(imageUtils.getImageURLs(productVariationId + "_", "products"));
        viewProductVariationDTO.setProductDescription(product.getDescription());
        viewProductVariationDTO.setProductName(product.getName());
        viewProductVariationDTO.setBrand(product.getBrand());
        viewProductVariationDTO.setSellerId(product.getSeller().getId());
        viewProductVariationDTO.setCategoryName(product.getCategory().getName());
        viewProductVariationDTO.setCategoryId(product.getCategory().getId());
        viewProductVariationDTO.setParentCategoryId(product.getCategory().getParentCategory().getId());
        return new ResponseEntity<>(viewProductVariationDTO, HttpStatus.OK);
    }

    public ResponseEntity<List<ViewProductSellerDTO>> viewAllProductsSeller(Integer pageSize, Integer pageOffset, String sortBy, String order, String query, Principal principal, Locale locale) {
        Seller seller = sellerRepository.findByEmail(principal.getName());
        Specification<Product> specification = ProductSpecifications.bySeller(seller.getId()).and(ProductSpecifications.isNotDeleted());
        if (query != null && !query.isBlank()) {
            specification = specification.and(ProductSpecifications.fromQueryString(query));
        }

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Integer sizee = productRepository.findAll().size();
        if (pageOffset < 0 || (double) pageOffset >= (double) sizee / (double) pageSize) {
            throw new PaginationError("invalid pageOffset");
        }
        Pageable pageable = PageRequest.of(pageOffset, pageSize, sort);

        List<Product> products = productRepository.findAll(specification, pageable).getContent();
        if (products.isEmpty()) {
            throw new InvalidParametersException("no products found");
        }
        products = products.stream().filter(product -> product.getSeller().getId().equals(seller.getId())).toList();

        List<ViewProductSellerDTO> viewProductSellerDTOList = products.stream().map(product -> {
            ViewProductSellerDTO viewProductSellerDTO = new ViewProductSellerDTO();
            viewProductSellerDTO.setBrand(product.getBrand());
            viewProductSellerDTO.setCategoryId(product.getCategory().getId());
            viewProductSellerDTO.setCategoryName(product.getCategory().getName());
            viewProductSellerDTO.setName(product.getName());
            viewProductSellerDTO.setDescription(product.getDescription());
            viewProductSellerDTO.setIsCancellable(product.getIsCancellable());
            viewProductSellerDTO.setIsReturnable(product.getIsReturnable());
            viewProductSellerDTO.setSellerId(product.getSeller().getId());
            viewProductSellerDTO.setId(product.getId());
            viewProductSellerDTO.setIsActive(product.getIsActive());
            viewProductSellerDTO.setIsDeleted(product.getIsDeleted());
            return viewProductSellerDTO;
        }).toList();

        return new ResponseEntity<>(viewProductSellerDTOList, HttpStatus.OK);

    }

    public ResponseEntity<List<ViewProductVariationDTO>> viewAllProductVariations(String productId, Integer pageSize, Integer
            pageOffset, String sortBy, String order, String query, Principal principal, Locale locale) {
        Seller seller = sellerRepository.findByEmail(principal.getName());
        Product product = productRepository.findById(productId);
        if (seller == null) {
            throw new InvalidParametersException("seller not found");
        }
        if (product == null) {
            throw new InvalidParametersException("product not found");
        }
        if (product.getIsDeleted()) {
            throw new InvalidParametersException("product should not be deleted.");
        }
        if (!seller.getId().equals(product.getSeller().getId())) {
            throw new InvalidParametersException(" This product is not added by  this seller.");
        }
        List<ViewProductVariationDTO> viewProductVariationDTOList = new ArrayList<>();
        Page<ProductVariation> sellerProductVariations;
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(Sort.Direction.fromString(order), sortBy));
        if (query != null && !query.isBlank()) {
            Specification<ProductVariation> specification = ProductVariationSpecifications.fromQueryString(query);
            sellerProductVariations = productVariationRepository.findAll(specification, pageable);
        } else {
            sellerProductVariations = productVariationRepository.findAll(pageable);
        }
        List<ProductVariation> productVariations = sellerProductVariations.getContent();
        productVariations = productVariations.stream().filter(productVariation -> productVariation.getProduct().getId().equals(productId)).toList();

        productVariations.stream().map(productVariation -> {
            ViewProductVariationDTO viewProductVariationDTO = new ViewProductVariationDTO();
            viewProductVariationDTO.setPrice(productVariation.getPrice());
            viewProductVariationDTO.setQuantityAvailable(productVariation.getQuantityAvailable());
            viewProductVariationDTO.setMetadata(productVariation.getMetadata());
            viewProductVariationDTO.setProductId(product.getId());
            viewProductVariationDTO.setId(productVariation.getId());
            viewProductVariationDTO.setPrimaryImageName(imageUtils.getImageURL("primary_" + productVariation.getId(), "products"));
            viewProductVariationDTO.setSecondaryImageNames(imageUtils.getImageURLs(productVariation.getId() + "_", "products"));
            viewProductVariationDTO.setProductDescription(product.getDescription());
            viewProductVariationDTO.setProductName(product.getName());
            viewProductVariationDTO.setBrand(product.getBrand());
            viewProductVariationDTO.setSellerId(product.getSeller().getId());
            viewProductVariationDTO.setCategoryName(product.getCategory().getName());
            viewProductVariationDTO.setCategoryId(product.getCategory().getId());
            viewProductVariationDTO.setParentCategoryId(product.getCategory().getParentCategory().getId());
            viewProductVariationDTO.setVariationIsActive(productVariation.getIsActive());
            viewProductVariationDTOList.add(viewProductVariationDTO);
            return viewProductVariationDTO;
        }).toList();
        return new ResponseEntity<>(viewProductVariationDTOList, HttpStatus.OK);

    }

    public ResponseEntity<String> deleteProduct(String productId, Principal principal, Locale locale) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new InvalidParametersException("product not found");
        }
        ;
        if (product.getIsDeleted()) {
            throw new InvalidParametersException("product already deleted.");
        }
        Seller seller = sellerRepository.findByEmail(principal.getName());
        if (!seller.getId().equals(product.getSeller().getId())) {
            throw new InvalidParametersException(" This product is not added by  this seller.");
        }
        product.setIsDeleted(true);
        productRepository.save(product);
        return new ResponseEntity<>("product deleted successfully", HttpStatus.OK);
    }

    public ResponseEntity<String> updateProduct(@Valid UpdateProductDTO updateProductDTO, Principal principal, Locale locale) {
        Product product = productRepository.findById(updateProductDTO.getProductId());
        if (product == null) {
            throw new InvalidParametersException("product not found");
        }

        if (product.getIsDeleted()) {
            throw new InvalidParametersException("product is deleted,can't update.");
        }
        Seller seller = sellerRepository.findByEmail(principal.getName());
        if (!seller.getId().equals(product.getSeller().getId())) {
            throw new InvalidParametersException(" This product is not added by  this seller.");
        }
        if (updateProductDTO.getName() != null) {
            product.setName(updateProductDTO.getName());
        }
        if (updateProductDTO.getDescription() != null) {
            product.setDescription(updateProductDTO.getDescription());
        }
        if (updateProductDTO.getIsCancellable() != null) {
            product.setIsCancellable(updateProductDTO.getIsCancellable());
        }
        if (updateProductDTO.getIsReturnable() != null) {
            product.setIsReturnable(updateProductDTO.getIsReturnable());
        }
        productRepository.save(product);
        return new ResponseEntity<>("product updated successfully", HttpStatus.OK);

    }

    public ResponseEntity<String> updateProductVariation(@Valid UpdateProductVariationDTO updateProductVariationDTO, Principal principal, Locale locale) {

        ProductVariation productVariation = productVariationRepository.findById(updateProductVariationDTO.getProductVariationId());
        if (productVariation == null) {
            throw new InvalidParametersException("product variation not found");
        }

        Product product = productRepository.findById(productVariation.getProduct().getId());
        if (product.getIsDeleted()) {
            throw new InvalidParametersException("product should not be deleted.");
        }
        if (!product.getIsActive()) {
            throw new InvalidParametersException("product should be Active.");
        }
        Seller seller = sellerRepository.findByEmail(principal.getName());
        if (!seller.getId().equals(product.getSeller().getId())) {
            throw new InvalidParametersException(" This product is not added by  this seller.");
        }
        if (updateProductVariationDTO.getPrice() != null) {
            productVariation.setPrice(updateProductVariationDTO.getPrice());
        }
        if (updateProductVariationDTO.getQuantityAvailable() != null) {
            productVariation.setQuantityAvailable(updateProductVariationDTO.getQuantityAvailable());
        }
        if (updateProductVariationDTO.getMetadata() != null) {
            Map<String, Object> metadata;
            try {
                metadata = new ObjectMapper().readValue(updateProductVariationDTO.getMetadata(), new TypeReference<>() {
                });
            } catch (IOException e) {
                throw new InvalidParametersException("Invalid metadata JSON format or invalid fields.");

            }
            metadata.forEach((key, value) -> {
                if (categoryMetadataFieldRepository.findByName(key) == null) {
                    throw new InvalidParametersException("Invalid metadata field: " + key);
                }
                ;
                CategoryMetadataFieldValues categoryMetadataFieldValues = categoryMetadataFieldValuesRepository.findByCategory_IdAndCategoryMetadataField_Id(product.getCategory().getId(), categoryMetadataFieldRepository.findByName(key).getId());
                if (categoryMetadataFieldValues == null) {
                    throw new InvalidParametersException("no metadata field values exists for this metadatafield: " + key);
                }
                ;
                if (!categoryMetadataFieldValues.getCategoryMetadataValues().contains(((String) value))) {
                    throw new InvalidParametersException("Invalid metadata field value for field: " + key);

                }
                metadata.put(key, value);
            });
            productVariation.setMetadata(metadata);
        }

        String productId = product.getId();
        String imageId = "primary_" + productVariation.getId();
        if (updateProductVariationDTO.getPrimaryImage() != null) {
            imageUtils.saveImageOnServer(updateProductVariationDTO.getPrimaryImage(), imageId, "products");
        }
        productVariation.setQuantityAvailable(updateProductVariationDTO.getQuantityAvailable());
        if (updateProductVariationDTO.getSecondaryImages() != null) {
            for (MultipartFile secondaryImage : updateProductVariationDTO.getSecondaryImages()) {
                imageUtils.saveImageOnServer(secondaryImage, productVariation.getId() + "_" + ((int) (Math.random() * 1000)), "products");
            }
        }
        productVariation.setPrimaryImageName(imageId);
        productVariationRepository.save(productVariation);
        return new ResponseEntity<>("Product variation updated successfully", HttpStatus.OK);
    }

    public ResponseEntity<ViewProductCustomerDTO> viewProductCustomer(String productId, Locale locale) {
        ViewProductCustomerDTO viewProductCustomerDTO = new ViewProductCustomerDTO();
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new InvalidParametersException("product not found");
        }
        ;
        if (product.getIsDeleted()) {
            throw new InvalidParametersException("product should not be deleted.");
        }
        Category category = product.getCategory();
        viewProductCustomerDTO.setCategoryId(category.getId());
        viewProductCustomerDTO.setCategoryName(category.getName());
        viewProductCustomerDTO.setParentCategoryId(category.getParentCategory().getId());
        viewProductCustomerDTO.setName(product.getName());
        viewProductCustomerDTO.setDescription(product.getDescription());
        viewProductCustomerDTO.setBrand(product.getBrand());
        viewProductCustomerDTO.setIsCancellable(product.getIsCancellable());
        viewProductCustomerDTO.setIsReturnable(product.getIsReturnable());
        viewProductCustomerDTO.setSellerId(product.getSeller().getId());
        viewProductCustomerDTO.setCompanyName(product.getSeller().getCompanyName());
        viewProductCustomerDTO.setId(product.getId());
        viewProductCustomerDTO.setIsActive(product.getIsActive());
        List<ProductVariation> productVariations = productVariationRepository.findByProductId(productId);
        List<ViewProductVariationDTO> viewProductVariationDTOList = new ArrayList<>();
        productVariations.stream().forEach(productVariation -> {
            ViewProductVariationDTO viewProductVariationDTO = new ViewProductVariationDTO();
            viewProductVariationDTO.setPrice(productVariation.getPrice());
            viewProductVariationDTO.setQuantityAvailable(productVariation.getQuantityAvailable());
            viewProductVariationDTO.setMetadata(productVariation.getMetadata());
            viewProductVariationDTO.setProductId(product.getId());
            viewProductVariationDTO.setId(productVariation.getId());
            viewProductVariationDTO.setPrimaryImageName(imageUtils.getImageURL("primary_" + productVariation.getId(), "products"));
            viewProductVariationDTO.setSecondaryImageNames(imageUtils.getImageURLs(productVariation.getId() + "_", "products"));
            viewProductVariationDTO.setProductDescription(product.getDescription());
            viewProductVariationDTO.setProductName(product.getName());
            viewProductVariationDTO.setBrand(product.getBrand());
            viewProductVariationDTO.setSellerId(product.getSeller().getId());
            viewProductVariationDTO.setCategoryName(product.getCategory().getName());
            viewProductVariationDTO.setCategoryId(product.getCategory().getId());
            viewProductVariationDTO.setParentCategoryId(product.getCategory().getParentCategory().getId());
            viewProductVariationDTOList.add(viewProductVariationDTO);
        });
        viewProductCustomerDTO.setProductVariations(viewProductVariationDTOList);
        return new ResponseEntity<>(viewProductCustomerDTO, HttpStatus.OK);
    }

    public ResponseEntity<List<ViewAllProductsCustomerDTO>> viewAllProductsCustomer(String categoryId, Integer pageSize, Integer pageOffset, String sortBy, String order, String query, Locale locale) {
        Category category = categoryRepository.findById(categoryId);
        if (category == null) {
            throw new InvalidParametersException("category not found");
        }
        List<String>categoryIds = new ArrayList<>();
        Specification<Product> specification;
        if (category.getIsLeaf()) {
            categoryIds.add(category.getId());
            specification = ProductSpecifications.byCategoryId(categoryIds).and(ProductSpecifications.isNotDeleted().and(ProductSpecifications.isActive()));
            if (query != null && !query.isBlank()) {
                specification = specification.and(ProductSpecifications.fromQueryString(query));
            }
        } else {
            categoryIds=getAllSubCategoryIds(category);
            specification = ProductSpecifications.byCategoryId(categoryIds).and(ProductSpecifications.isNotDeleted().and(ProductSpecifications.isActive()));
            if (query != null && !query.isBlank()) {
                specification = specification.and(ProductSpecifications.fromQueryString(query));
            }
        }

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Integer sizee = productRepository.findAll().size();
        if (pageOffset < 0 || (double) pageOffset >= (double) sizee / (double) pageSize) {
            throw new PaginationError("invalid pageOffset");
        }
        Pageable pageable = PageRequest.of(pageOffset, pageSize, sort);

        List<Product> products = productRepository.findAll(specification, pageable).getContent();
        if (products.isEmpty()) {
            throw new InvalidParametersException("no products found");
        }
        products = products.stream().filter(product -> product.getProductVariations().size() > 0).toList();
        List<ViewAllProductsCustomerDTO> viewAllProductsCustomerDTOList = new ArrayList<>();
        products.stream().forEach(product -> {
            ViewAllProductsCustomerDTO viewAllProductsCustomerDTO = new ViewAllProductsCustomerDTO();
            viewAllProductsCustomerDTO.setBrand(product.getBrand());
            viewAllProductsCustomerDTO.setCategoryId(product.getCategory().getId());
            viewAllProductsCustomerDTO.setCategoryName(product.getCategory().getName());
            viewAllProductsCustomerDTO.setName(product.getName());
            viewAllProductsCustomerDTO.setDescription(product.getDescription());
            viewAllProductsCustomerDTO.setIsCancellable(product.getIsCancellable());
            viewAllProductsCustomerDTO.setIsReturnable(product.getIsReturnable());
            viewAllProductsCustomerDTO.setSellerId(product.getSeller().getId());
            viewAllProductsCustomerDTO.setId(product.getId());
            viewAllProductsCustomerDTO.setParentCategoryId(product.getCategory().getParentCategory().getId());
            viewAllProductsCustomerDTO.setCompanyName(product.getSeller().getCompanyName());
            viewAllProductsCustomerDTO.setIsActive(product.getIsActive());
            List<ProductVariation> productVariations = productVariationRepository.findByProductId(product.getId());
            List<String> primaryImageNames = new ArrayList<>();
            productVariations.stream().forEach(productVariation -> {
                primaryImageNames.add(imageUtils.getImageURL("primary_" + productVariation.getId(), "products"));
            });
            viewAllProductsCustomerDTO.setProductImages(primaryImageNames);
            viewAllProductsCustomerDTOList.add(viewAllProductsCustomerDTO);
        });
        return new ResponseEntity<>(viewAllProductsCustomerDTOList, HttpStatus.OK);
    }
    public ResponseEntity<List<ViewAllProductsCustomerDTO>> viewSimilarProductsCustomer(String productId, Integer pageSize, Integer pageOffset, String sortBy, String order, String query, Locale locale) {
        if (productId == null) {
            throw new InvalidParametersException("productId not found");
        }
        Product productt = productRepository.findById(productId);
        if (productt == null) {
            throw new InvalidParametersException("product not found");
        }
        Category category = productRepository.findById(productId).getCategory();
        if (category == null) {
            throw new InvalidParametersException("category not found");
        }
        List<String>categoryIds = new ArrayList<>();
        Specification<Product> specification;
            categoryIds.add(category.getId());
            specification = ProductSpecifications.byCategoryId(categoryIds).and(ProductSpecifications.isNotDeleted().and(ProductSpecifications.isActive()));
            if (query != null && !query.isBlank()) {
                specification = specification.and(ProductSpecifications.fromQueryString(query));
            }


        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Integer sizee = productRepository.findAll().size();
        if (pageOffset < 0 || (double) pageOffset >= (double) sizee / (double) pageSize) {
            throw new PaginationError("invalid pageOffset");
        }
        Pageable pageable = PageRequest.of(pageOffset, pageSize, sort);

        List<Product> products = productRepository.findAll(specification, pageable).getContent();
        if (products.isEmpty()) {
            throw new InvalidParametersException("no products found");
        }
        products = products.stream().filter(product -> product.getProductVariations().size() > 0).toList();
        List<ViewAllProductsCustomerDTO> viewAllProductsCustomerDTOList = new ArrayList<>();
        products.stream().forEach(product -> {
            ViewAllProductsCustomerDTO viewAllProductsCustomerDTO = new ViewAllProductsCustomerDTO();
            viewAllProductsCustomerDTO.setBrand(product.getBrand());
            viewAllProductsCustomerDTO.setCategoryId(product.getCategory().getId());
            viewAllProductsCustomerDTO.setCategoryName(product.getCategory().getName());
            viewAllProductsCustomerDTO.setName(product.getName());
            viewAllProductsCustomerDTO.setDescription(product.getDescription());
            viewAllProductsCustomerDTO.setIsCancellable(product.getIsCancellable());
            viewAllProductsCustomerDTO.setIsReturnable(product.getIsReturnable());
            viewAllProductsCustomerDTO.setSellerId(product.getSeller().getId());
            viewAllProductsCustomerDTO.setId(product.getId());
            viewAllProductsCustomerDTO.setParentCategoryId(product.getCategory().getParentCategory().getId());
            viewAllProductsCustomerDTO.setCompanyName(product.getSeller().getCompanyName());
            viewAllProductsCustomerDTO.setIsActive(product.getIsActive());
            List<ProductVariation> productVariations = productVariationRepository.findByProductId(product.getId());
            List<String> primaryImageNames = new ArrayList<>();
            productVariations.stream().forEach(productVariation -> {
                primaryImageNames.add(imageUtils.getImageURL("primary_" + productVariation.getId(), "products"));
            });
            viewAllProductsCustomerDTO.setProductImages(primaryImageNames);
            viewAllProductsCustomerDTOList.add(viewAllProductsCustomerDTO);
        });
        return new ResponseEntity<>(viewAllProductsCustomerDTOList, HttpStatus.OK);
    }

    public ResponseEntity<ViewProductAdminDTO> viewProductAdmin(String productId, Locale locale) {
        ViewProductAdminDTO viewProductAdminDTO = new ViewProductAdminDTO();
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new InvalidParametersException("product not found");
        }
        ;
        if (product.getIsDeleted()) {
            throw new InvalidParametersException("product should not be deleted.");
        }

        viewProductAdminDTO.setCategoryId(product.getCategory().getId());
        viewProductAdminDTO.setCategoryName(product.getCategory().getName());
        viewProductAdminDTO.setParentCategoryId(product.getCategory().getParentCategory().getId());
        viewProductAdminDTO.setName(product.getName());
        viewProductAdminDTO.setDescription(product.getDescription());
        viewProductAdminDTO.setBrand(product.getBrand());
        viewProductAdminDTO.setIsCancellable(product.getIsCancellable());
        viewProductAdminDTO.setIsReturnable(product.getIsReturnable());
        viewProductAdminDTO.setSellerId(product.getSeller().getId());
        viewProductAdminDTO.setCompanyName(product.getSeller().getCompanyName());
        viewProductAdminDTO.setId(product.getId());
        viewProductAdminDTO.setIsActive(product.getIsActive());
        viewProductAdminDTO.setIsDeleted(product.getIsDeleted());
        List<ProductVariation> productVariations = productVariationRepository.findByProductId(productId);
        List<String> primaryImageNames = new ArrayList<>();
        productVariations.stream().forEach(productVariation -> {
            primaryImageNames.add(imageUtils.getImageURL("primary_" + productVariation.getId(), "products"));
        });
        viewProductAdminDTO.setProductImages(primaryImageNames);

        return new ResponseEntity<>(viewProductAdminDTO, HttpStatus.OK);
    }
  public   List<String> getAllSubCategoryIds(Category category){
        List<String> leafCategoryIds = new ArrayList<>();
        Queue<Category> queue = new LinkedList<>();
        queue.add(category);
        while (!queue.isEmpty()){
            Category currentCategory = queue.poll();

            if(currentCategory.getIsLeaf()){
                leafCategoryIds.add(currentCategory.getId());
            }
            else{

                queue.addAll(categoryRepository.findAllByParentCategoryId(currentCategory.getId()));
            }
        }
        return leafCategoryIds;
    }
    public ResponseEntity<List<ViewProductAdminDTO>> viewAllProductsAdmin(Integer pageSize, Integer pageOffset, String sortBy, String order, String query, Locale locale) {
        Specification<Product> specification = ProductSpecifications.isNotDeleted().and(ProductSpecifications.isActive());
        if (query != null && !query.isBlank()) {
            specification = specification.and(ProductSpecifications.fromQueryString(query));
        }

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Integer sizee = productRepository.findAll().size();
        if (pageOffset < 0 || (double) pageOffset >= (double) sizee / (double) pageSize) {
            throw new PaginationError("invalid pageOffset");
        }
        Pageable pageable = PageRequest.of(pageOffset, pageSize, sort);

        List<Product> products = productRepository.findAll(specification, pageable).getContent();
        if (products.isEmpty()) {
            throw new InvalidParametersException("no products found");
        }

        List<ViewProductAdminDTO> viewProductAdminDTOList = products.stream().map(product -> {
            ViewProductAdminDTO viewProductAdminDTO = new ViewProductAdminDTO();
            viewProductAdminDTO.setBrand(product.getBrand());
            viewProductAdminDTO.setCategoryId(product.getCategory().getId());
            viewProductAdminDTO.setCategoryName(product.getCategory().getName());
            viewProductAdminDTO.setName(product.getName());
            viewProductAdminDTO.setDescription(product.getDescription());
            viewProductAdminDTO.setIsCancellable(product.getIsCancellable());
            viewProductAdminDTO.setIsReturnable(product.getIsReturnable());
            viewProductAdminDTO.setSellerId(product.getSeller().getId());
            viewProductAdminDTO.setId(product.getId());
            viewProductAdminDTO.setIsActive(product.getIsActive());
            viewProductAdminDTO.setIsDeleted(product.getIsDeleted());
            viewProductAdminDTO.setParentCategoryId(product.getCategory().getParentCategory().getId());
            viewProductAdminDTO.setCompanyName(product.getSeller().getCompanyName());
            List<ProductVariation> productVariations = productVariationRepository.findByProductId(product.getId());
            List<String> primaryImageNames = new ArrayList<>();
            productVariations.stream().forEach(productVariation -> {
                primaryImageNames.add(imageUtils.getImageURL("primary_" + productVariation.getId(), "products"));
            });
            viewProductAdminDTO.setProductImages(primaryImageNames);
            return viewProductAdminDTO;
        }).toList();

        return new ResponseEntity<>(viewProductAdminDTOList, HttpStatus.OK);

    }
public     ResponseEntity<String> deactivateProduct(String productId, Locale locale){
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new InvalidParametersException("product not found");
        }
        ;
        if (product.getIsDeleted()) {
            throw new InvalidParametersException("product should not be deleted.");
        }
        if(!product.getIsActive()){
            throw new InvalidParametersException("product already deactivated.");
        }
    try {
        emailService.sendEmail(product.getSeller().getEmail(),"Product(product id: "+productId+") Deactivation Notification",
                "Product details:<br>" +
                        "Product ID: " + product.getId() + "<br>" +
                        "Product Name: " + product.getName() + "<br>" +
                        "Brand: " + product.getBrand() + "<br>" +
                        "Category: " + product.getCategory().getName() + "<br>" +
                        "Description: " + product.getDescription() + "<br>" +
                        "Is Cancellable: " + product.getIsCancellable() + "<br>" +
                        "Is Returnable: " + product.getIsReturnable());

    } catch (MessagingException e) {
        return new ResponseEntity<>("Error sending email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
        product.setIsActive(false);
        productRepository.save(product);
        return new ResponseEntity<>("product deactivated successfully", HttpStatus.OK);
}
    public     ResponseEntity<String> activateProduct(String productId, Locale locale){
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new InvalidParametersException("product not found");
        }
        ;
        if (product.getIsDeleted()) {
            throw new InvalidParametersException("product should not be deleted.");
        }
        if(product.getIsActive()){
            throw new InvalidParametersException("product already activated.");
        }
        try {
            emailService.sendEmail(product.getSeller().getEmail(),"Product(product id: "+productId+") activation Notification",
                    "Product details:<br>" +
                            "Product ID: " + product.getId() + "<br>" +
                            "Product Name: " + product.getName() + "<br>" +
                            "Brand: " + product.getBrand() + "<br>" +
                            "Category: " + product.getCategory().getName() + "<br>" +
                            "Description: " + product.getDescription() + "<br>" +
                            "Is Cancellable: " + product.getIsCancellable() + "<br>" +
                            "Is Returnable: " + product.getIsReturnable());

        } catch (MessagingException e) {
            return new ResponseEntity<>("Error sending email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        product.setIsActive(true);
        productRepository.save(product);
        return new ResponseEntity<>("product activated successfully", HttpStatus.OK);
    }

    }