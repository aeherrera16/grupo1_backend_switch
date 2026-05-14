package com.banquito.core.controller;

import com.banquito.core.dto.CoreUserAuthResponseDTO;
import com.banquito.core.dto.CreateCoreUserRequestDTO;
import com.banquito.core.dto.CreateWebCredentialRequestDTO;
import com.banquito.core.dto.CustomerAuthResponseDTO;
import com.banquito.core.dto.LoginRequestDTO;
import com.banquito.core.model.CoreUser;
import com.banquito.core.model.Customer;
import com.banquito.core.repository.CoreUserRepository;
import com.banquito.core.repository.CustomerRepository;
import com.banquito.core.service.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/core/v1/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174"})
@RequiredArgsConstructor
public class AuthenticationController {

    private static final String CORE_USER_HEADER = "X-Core-User-Id";

    private final IAuthenticationService authenticationService;
    private final CoreUserRepository coreUserRepository;
    private final CustomerRepository customerRepository;

    @PostMapping("/customers/login")
    public ResponseEntity<CustomerAuthResponseDTO> loginCustomer(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authenticationService.authenticateCustomer(request));
    }

    @PostMapping("/core-users/login")
    public ResponseEntity<CoreUserAuthResponseDTO> loginCoreUser(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authenticationService.authenticateCoreUser(request));
    }

    @PostMapping("/customers/credentials")
    public ResponseEntity<CustomerAuthResponseDTO> createWebCredential(
            @RequestBody CreateWebCredentialRequestDTO request,
            @RequestHeader(CORE_USER_HEADER) Integer coreUserId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationService.createWebCredential(request, coreUserId));
    }

    @PostMapping("/core-users")
    public ResponseEntity<CoreUserAuthResponseDTO> createCoreUser(
            @RequestBody CreateCoreUserRequestDTO request,
            @RequestHeader(CORE_USER_HEADER) Integer coreUserId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationService.createCoreUser(request, coreUserId));
    }

    @GetMapping("/test-data")
    public ResponseEntity<Map<String, Object>> getTestData() {
        Map<String, Object> data = new HashMap<>();
        CoreUser adminCore = coreUserRepository.findByUsername("admin.core").orElse(null);
        List<Customer> customers = customerRepository.findAll();

        data.put("coreUserId", adminCore != null ? adminCore.getId() : null);
        data.put("customers", customers.stream()
                .map(c -> new HashMap<String, Object>() {{
                    put("id", c.getId());
                    put("name", c.getFirstName() + " " + c.getLastName());
                    put("email", c.getEmail());
                }})
                .toList());

        return ResponseEntity.ok(data);
    }
}
