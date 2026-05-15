package com.banquito.core.service;

import com.banquito.core.dto.CustomerSubtypeResponseDTO;

import java.util.List;

public interface ICustomerSubtypeService {

    List<CustomerSubtypeResponseDTO> findAll();

    CustomerSubtypeResponseDTO findById(Integer id);

    List<CustomerSubtypeResponseDTO> findByCustomerType(String customerType);
}
