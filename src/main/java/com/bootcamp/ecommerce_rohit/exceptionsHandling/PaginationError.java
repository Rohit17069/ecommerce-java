package com.bootcamp.ecommerce_rohit.exceptionsHandling;

public class PaginationError extends RuntimeException{
    public PaginationError(String message) {
        super(message);
}}
