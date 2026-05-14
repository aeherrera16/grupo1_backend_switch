package com.banquito.core.repository;

import com.banquito.core.enums.CustomerSubtypeStatusEnum;
import com.banquito.core.model.CustomerSubtype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerSubtypeRepository extends JpaRepository<CustomerSubtype, Integer> {
    Optional<CustomerSubtype> findByName(String name);
    Optional<CustomerSubtype> findByStatus(CustomerSubtypeStatusEnum status);
}
