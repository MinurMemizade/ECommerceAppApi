package com.company.ecommercebackend.service;

import com.company.ecommercebackend.model.Product;
import com.company.ecommercebackend.model.dao.ProductDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

  private ProductDAO productDAO;

  public ProductService(ProductDAO productDAO) {
    this.productDAO = productDAO;
  }

  public List<Product> getProducts() {
    return productDAO.findAll();
  }

}
