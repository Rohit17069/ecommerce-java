package com.bootcamp.ecommerce_rohit.specifications;

import com.bootcamp.ecommerce_rohit.entities.ProductVariation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductVariationSpecifications {

    public static Specification<ProductVariation> fromQueryString(String queryString) {
        if (queryString == null || queryString.trim().isEmpty()) {
            return Specification.where(null);
        }

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String[] filters = queryString.split(",");

            for (String filter : filters) {
                String[] parts = filter.split(":");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    String value = parts[1].trim();
                    try {
                        predicates.add(cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%"));
                    } catch (IllegalArgumentException e) {
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
