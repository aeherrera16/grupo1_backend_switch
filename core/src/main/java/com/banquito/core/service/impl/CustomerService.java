package com.banquito.core.service.impl;

import com.banquito.core.dto.CustomerRequestDTO;
import com.banquito.core.dto.CustomerResponseDTO;
import com.banquito.core.enums.CustomerStatusEnum;
import com.banquito.core.enums.CustomerTypeEnum;
import com.banquito.core.exception.CustomerNotFoundException;
import com.banquito.core.model.Customer;
import com.banquito.core.model.CustomerSubtype;
import com.banquito.core.repository.CustomerRepository;
import com.banquito.core.repository.CustomerSubtypeRepository;
import com.banquito.core.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerSubtypeRepository customerSubtypeRepository;

    @Transactional(readOnly = true)
    @Override
    public List<CustomerResponseDTO> findAll() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public CustomerResponseDTO findById(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(String.valueOf(id)));
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    @Override
    public CustomerResponseDTO findByIdentification(String identificationType, String identification) {
        Customer customer = customerRepository.findByIdentificationTypeAndIdentification(identificationType, identification)
                .orElseThrow(() -> new CustomerNotFoundException(identification));
        return toResponse(customer);
    }

    @Transactional
    @Override
    public CustomerResponseDTO create(CustomerRequestDTO request) {
        validateCustomerRequest(request);

        CustomerSubtype subtype = customerSubtypeRepository.findById(request.getCustomerSubtypeId())
                .orElseThrow(() -> new RuntimeException(
                        "Subtipo de cliente no encontrado: " + request.getCustomerSubtypeId()));

        Customer customer = new Customer();
        customer.setCustomerSubtype(subtype);
        customer.setCustomerType(request.getCustomerType());
        customer.setIdentificationType(request.getIdentificationType());
        customer.setIdentification(request.getIdentification());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setBirthDate(request.getBirthDate());
        customer.setLegalName(request.getLegalName());
        customer.setConstitutionDate(request.getConstitutionDate());

        if (request.getLegalRepresentativeId() != null) {
            customer.setLegalRepresentative(customerRepository.findById(request.getLegalRepresentativeId())
                    .orElseThrow(() -> new CustomerNotFoundException(
                            String.valueOf(request.getLegalRepresentativeId()))));
        }

        customer.setEmail(request.getEmail());
        customer.setMobilePhone(request.getMobilePhone());
        customer.setAddress(request.getAddress());
        customer.setStatus(CustomerStatusEnum.ACTIVO);
        customer.setRegistrationDate(LocalDateTime.now());

        log.info("Creando cliente con identificación: {}", customer.getIdentification());
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    @Override
    public CustomerResponseDTO update(Integer id, CustomerRequestDTO request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(String.valueOf(id)));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            customer.setEmail(request.getEmail());
        }

        if (request.getMobilePhone() != null && !request.getMobilePhone().isBlank()) {
            customer.setMobilePhone(request.getMobilePhone());
        }

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            customer.setAddress(request.getAddress());
        }

        log.info("Actualizando datos del cliente con ID: {}", id);
        return toResponse(customerRepository.save(customer));
    }

    private void validateCustomerRequest(CustomerRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La información del cliente es obligatoria");
        }

        if (request.getCustomerSubtypeId() == null) {
            throw new IllegalArgumentException("El subtipo de cliente es obligatorio");
        }

        if (request.getCustomerType() == null) {
            throw new IllegalArgumentException("El tipo de cliente es obligatorio");
        }

        if (isBlank(request.getIdentificationType())) {
            throw new IllegalArgumentException("El tipo de identificación es obligatorio");
        }

        if (isBlank(request.getIdentification())) {
            throw new IllegalArgumentException("La identificación es obligatoria");
        }

        if (request.getCustomerType() == CustomerTypeEnum.NATURAL) {
            validateNaturalCustomer(request);
            return;
        }

        if (request.getCustomerType() == CustomerTypeEnum.JURIDICO) {
            validateLegalCustomer(request);
            return;
        }

        throw new IllegalArgumentException("Tipo de cliente no permitido");
    }

    private void validateNaturalCustomer(CustomerRequestDTO request) {
        if (!"CEDULA".equalsIgnoreCase(request.getIdentificationType())) {
            throw new IllegalArgumentException("Una Persona Natural debe registrar identificación tipo CEDULA");
        }

        if (isBlank(request.getFirstName())) {
            throw new IllegalArgumentException("Una Persona Natural debe registrar nombres");
        }

        if (isBlank(request.getLastName())) {
            throw new IllegalArgumentException("Una Persona Natural debe registrar apellidos");
        }

        if (request.getBirthDate() == null) {
            throw new IllegalArgumentException("Una Persona Natural debe registrar fecha de nacimiento");
        }
    }

    private void validateLegalCustomer(CustomerRequestDTO request) {
        if (!"RUC".equalsIgnoreCase(request.getIdentificationType())) {
            throw new IllegalArgumentException("Una Persona Jurídica debe registrar identificación tipo RUC");
        }

        if (isBlank(request.getLegalName())) {
            throw new IllegalArgumentException("Una Persona Jurídica debe registrar razón social");
        }

        if (request.getConstitutionDate() == null) {
            throw new IllegalArgumentException("Una Persona Jurídica debe registrar fecha de constitución");
        }

        if (request.getLegalRepresentativeId() == null) {
            throw new IllegalArgumentException("Una Persona Jurídica debe tener representante legal asignado");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private CustomerResponseDTO toResponse(Customer customer) {
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getCustomerType(),
                customer.getIdentificationType(),
                customer.getIdentification(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getMobilePhone(),
                customer.getAddress(),
                customer.getStatus()
        );
    }
}