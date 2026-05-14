package com.banquito.core.service;

import com.banquito.core.dto.CustomerRequestDTO;
import com.banquito.core.dto.CustomerResponseDTO;

import java.util.List;

public interface ICustomerService {

    List<CustomerResponseDTO> findAll();

    CustomerResponseDTO findById(Integer id);

    CustomerResponseDTO findByIdentification(String identificationType, String identification);

    CustomerResponseDTO create(CustomerRequestDTO request);
}
