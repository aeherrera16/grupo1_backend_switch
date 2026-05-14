package com.banquito.core.controller;

import com.banquito.core.dto.CustomerRequestDTO;
import com.banquito.core.dto.CustomerResponseDTO;
import com.banquito.core.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/core/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final ICustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> findAll() {
        return ResponseEntity.ok(customerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @GetMapping("/identification/{type}/{number}")
    public ResponseEntity<CustomerResponseDTO> findByIdentification(
            @PathVariable String type,
            @PathVariable String number) {
        return ResponseEntity.ok(customerService.findByIdentification(type, number));
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(@RequestBody CustomerRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }
}
