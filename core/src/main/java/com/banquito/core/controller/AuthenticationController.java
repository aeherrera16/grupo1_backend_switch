package com.banquito.core.controller;

import com.banquito.core.dto.CoreUserAuthResponseDTO;
import com.banquito.core.dto.CustomerAuthResponseDTO;
import com.banquito.core.dto.LoginRequestDTO;
import com.banquito.core.service.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    @PostMapping("/customers/login")
    public ResponseEntity<CustomerAuthResponseDTO> loginCustomer(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authenticationService.authenticateCustomer(request));
    }

    @PostMapping("/core-users/login")
    public ResponseEntity<CoreUserAuthResponseDTO> loginCoreUser(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authenticationService.authenticateCoreUser(request));
    }
}
