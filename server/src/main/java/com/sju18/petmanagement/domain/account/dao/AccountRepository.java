package com.sju18.petmanagement.domain.account.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
    boolean existsByUsername(String username);
    @Transactional
    void deleteByUsername(String username);
}
