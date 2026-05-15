package com.banquito.core.controller;

import com.banquito.core.dto.CustomerSubtypeResponseDTO;
import com.banquito.core.service.ICustomerSubtypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/core/v1/customer-subtypes")
@RequiredArgsConstructor
public class CustomerSubtypeController {

    private final ICustomerSubtypeService customerSubtypeService;

    @GetMapping
    public ResponseEntity<List<CustomerSubtypeResponseDTO>> findAll() {
        return ResponseEntity.ok(customerSubtypeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerSubtypeResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(customerSubtypeService.findById(id));
    }

    @GetMapping("/type/{customerType}")
    public ResponseEntity<List<CustomerSubtypeResponseDTO>> findByCustomerType(
            @PathVariable String customerType) {
        return ResponseEntity.ok(customerSubtypeService.findByCustomerType(customerType));
    }
}
