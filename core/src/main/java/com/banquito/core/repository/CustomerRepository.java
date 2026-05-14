package com.banquito.core.repository;

import com.banquito.core.enums.CustomerStatusEnum;
import com.banquito.core.enums.CustomerTypeEnum;
import com.banquito.core.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByIdentification(String identification);
    Optional<Customer> findByIdentificationTypeAndIdentification(String identificationType, String identification);
    List<Customer> findByCustomerType(CustomerTypeEnum customerType);
    List<Customer> findByStatus(CustomerStatusEnum status);
}
