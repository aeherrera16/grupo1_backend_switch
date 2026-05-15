package com.banquito.core.service.impl;

import com.banquito.core.dto.CoreUserAuthResponseDTO;
import com.banquito.core.dto.CreateCoreUserRequestDTO;
import com.banquito.core.dto.CreateWebCredentialRequestDTO;
import com.banquito.core.dto.CustomerAuthResponseDTO;
import com.banquito.core.dto.LoginRequestDTO;
import com.banquito.core.enums.CommonStatusEnum;
import com.banquito.core.exception.AutenticacionException;
import com.banquito.core.model.CoreUser;
import com.banquito.core.model.Customer;
import com.banquito.core.model.WebCredential;
import com.banquito.core.repository.CoreUserRepository;
import com.banquito.core.repository.CustomerRepository;
import com.banquito.core.repository.WebCredentialRepository;
import com.banquito.core.service.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final WebCredentialRepository webCredentialRepository;
    private final CoreUserRepository coreUserRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CustomerAuthResponseDTO authenticateCustomer(LoginRequestDTO request) {
        WebCredential credential = webCredentialRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AutenticacionException("Credenciales invalidas"));
        if (credential.getStatus() != CommonStatusEnum.ACTIVO ||
                !passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new AutenticacionException("Credenciales invalidas");
        }

        credential.setLastLogin(LocalDateTime.now());
        return toCustomerAuthResponse(webCredentialRepository.save(credential));
    }

    @Override
    @Transactional
    public CoreUserAuthResponseDTO authenticateCoreUser(LoginRequestDTO request) {
        CoreUser coreUser = coreUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AutenticacionException("Credenciales invalidas"));
        if (coreUser.getStatus() != CommonStatusEnum.ACTIVO ||
                !passwordEncoder.matches(request.getPassword(), coreUser.getPasswordHash())) {
            throw new AutenticacionException("Credenciales invalidas");
        }

        coreUser.setLastLogin(LocalDateTime.now());
        return toCoreUserAuthResponse(coreUserRepository.save(coreUser));
    }

    @Override
    @Transactional
    public CustomerAuthResponseDTO createWebCredential(CreateWebCredentialRequestDTO request, Integer coreUserId) {
        validateActiveCoreUser(coreUserId);
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + request.getCustomerId()));
        webCredentialRepository.findByUsername(request.getUsername()).ifPresent(existing -> {
            throw new IllegalArgumentException("Username ya existe: " + request.getUsername());
        });

        WebCredential credential = new WebCredential();
        credential.setCustomer(customer);
        credential.setUsername(request.getUsername());
        credential.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        credential.setStatus(CommonStatusEnum.ACTIVO);
        credential.setCreationDate(LocalDateTime.now());

        return toCustomerAuthResponse(webCredentialRepository.save(credential));
    }

    @Override
    @Transactional
    public CoreUserAuthResponseDTO createCoreUser(CreateCoreUserRequestDTO request, Integer coreUserId) {
        validateActiveCoreUser(coreUserId);
        coreUserRepository.findByUsername(request.getUsername()).ifPresent(existing -> {
            throw new IllegalArgumentException("Username ya existe: " + request.getUsername());
        });

        CoreUser coreUser = new CoreUser();
        coreUser.setUsername(request.getUsername());
        coreUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        coreUser.setFullName(request.getFullName());
        coreUser.setRole(request.getRole());
        coreUser.setStatus(CommonStatusEnum.ACTIVO);
        coreUser.setCreationDate(LocalDateTime.now());

        return toCoreUserAuthResponse(coreUserRepository.save(coreUser));
    }

    @Override
    @Transactional(readOnly = true)
    public void validateActiveCoreUser(Integer coreUserId) {
        CoreUser coreUser = coreUserRepository.findById(coreUserId)
                .orElseThrow(() -> new SecurityException("CoreUser no autorizado: " + coreUserId));
        if (coreUser.getStatus() != CommonStatusEnum.ACTIVO) {
            throw new SecurityException("CoreUser inactivo o bloqueado: " + coreUserId);
        }
    }

    private CustomerAuthResponseDTO toCustomerAuthResponse(WebCredential credential) {
        Customer customer = credential.getCustomer();
        return new CustomerAuthResponseDTO(
                credential.getId(),
                customer.getId(),
                credential.getUsername(),
                resolveCustomerName(customer),
                credential.getStatus(),
                credential.getLastLogin()
        );
    }

    private CoreUserAuthResponseDTO toCoreUserAuthResponse(CoreUser coreUser) {
        return new CoreUserAuthResponseDTO(
                coreUser.getId(),
                coreUser.getUsername(),
                coreUser.getFullName(),
                coreUser.getRole(),
                coreUser.getStatus(),
                coreUser.getLastLogin()
        );
    }

    private String resolveCustomerName(Customer customer) {
        if (customer.getLegalName() != null && !customer.getLegalName().isBlank()) {
            return customer.getLegalName();
        }
        return ((customer.getFirstName() != null ? customer.getFirstName() : "") + " " +
                (customer.getLastName() != null ? customer.getLastName() : "")).trim();
    }
}