package com.company.ecommercebackend.service;

import com.company.ecommercebackend.model.LocalUser;
import com.company.ecommercebackend.model.WebOrder;
import com.company.ecommercebackend.model.dao.WebOrderDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

  private WebOrderDAO webOrderDAO;

  public OrderService(WebOrderDAO webOrderDAO) {
    this.webOrderDAO = webOrderDAO;
  }

  public List<WebOrder> getOrders(LocalUser user) {
    return webOrderDAO.findByUser(user);
  }

}
