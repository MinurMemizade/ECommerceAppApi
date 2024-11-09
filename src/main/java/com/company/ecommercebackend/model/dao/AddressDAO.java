package com.company.ecommercebackend.model.dao;

import com.company.ecommercebackend.model.Address;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface AddressDAO extends ListCrudRepository<Address,Long> {

    Optional<Address> findUserById(Long id);

}
