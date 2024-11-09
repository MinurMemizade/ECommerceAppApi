package com.company.ecommercebackend.model.dao;

import com.company.ecommercebackend.model.LocalUser;
import com.company.ecommercebackend.model.VerificationToken;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;
public interface VerificationTokenDAO extends ListCrudRepository<VerificationToken, Long> {

  Optional<VerificationToken> findByToken(String token);

  void deleteByUser(LocalUser user);

}
