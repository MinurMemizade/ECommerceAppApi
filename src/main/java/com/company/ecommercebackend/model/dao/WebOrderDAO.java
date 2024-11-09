package com.company.ecommercebackend.model.dao;

import com.company.ecommercebackend.model.LocalUser;
import com.company.ecommercebackend.model.WebOrder;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
public interface WebOrderDAO extends ListCrudRepository<WebOrder, Long> {

  List<WebOrder> findByUser(LocalUser user);

}
