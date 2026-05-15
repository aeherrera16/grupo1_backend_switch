package com.banquito.core.service.impl;

import com.banquito.core.dto.CustomerSubtypeResponseDTO;
import com.banquito.core.model.CustomerSubtype;
import com.banquito.core.repository.CustomerSubtypeRepository;
import com.banquito.core.service.ICustomerSubtypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerSubtypeService implements ICustomerSubtypeService {

    private final CustomerSubtypeRepository customerSubtypeRepository;

    @Transactional(readOnly = true)
    @Override
    public List<CustomerSubtypeResponseDTO> findAll() {
        return customerSubtypeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public CustomerSubtypeResponseDTO findById(Integer id) {
        CustomerSubtype subtype = customerSubtypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CustomerSubtype no encontrado: " + id));
        return toResponse(subtype);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CustomerSubtypeResponseDTO> findByCustomerType(String customerType) {
        return customerSubtypeRepository.findByCustomerType(customerType).stream()
                .map(this::toResponse).toList();
    }

    private CustomerSubtypeResponseDTO toResponse(CustomerSubtype subtype) {
        return new CustomerSubtypeResponseDTO(
                subtype.getId(),
                subtype.getCustomerType(),
                subtype.getName(),
                subtype.getDescription(),
                subtype.getStatus() != null ? subtype.getStatus().getValue() : null,
                subtype.getObservations()
        );
    }
}
