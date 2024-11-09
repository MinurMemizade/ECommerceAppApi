package com.company.ecommercebackend.model.dao;

import com.company.ecommercebackend.model.Product;
import org.springframework.data.repository.ListCrudRepository;
public interface ProductDAO extends ListCrudRepository<Product, Long> {
}
