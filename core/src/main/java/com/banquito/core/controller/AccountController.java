package com.banquito.core.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.core.dto.AccountRequestDTO;
import com.banquito.core.dto.AccountResponseDTO;
import com.banquito.core.dto.TransactionResponseDTO;
import com.banquito.core.service.IAccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174"})
@RequestMapping("/core/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private static final String CORE_USER_HEADER = "X-Core-User-Id";

    private final IAccountService accountService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponseDTO>> findByCustomerId(
            @PathVariable Integer customerId,
            @RequestHeader(CORE_USER_HEADER) Integer coreUserId) {
        return ResponseEntity.ok(accountService.findByCustomerId(customerId, coreUserId));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> findByAccountNumber(
            @PathVariable String accountNumber,
            @RequestHeader(CORE_USER_HEADER) Integer coreUserId) {
        return ResponseEntity.ok(accountService.findByAccountNumber(accountNumber, coreUserId));
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> create(
            @RequestBody AccountRequestDTO request,
            @RequestHeader(CORE_USER_HEADER) Integer coreUserId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(request, coreUserId));
    }

    @PatchMapping("/{accountNumber}/inactivate")
    public ResponseEntity<AccountResponseDTO> inactivate(
            @PathVariable String accountNumber,
            @RequestHeader(CORE_USER_HEADER) Integer coreUserId) {
        return ResponseEntity.ok(accountService.inactivate(accountNumber, coreUserId));
    }

    @PatchMapping("/{accountNumber}/block")
    public ResponseEntity<AccountResponseDTO> block(
            @PathVariable String accountNumber,
            @RequestHeader(CORE_USER_HEADER) Integer coreUserId) {
        return ResponseEntity.ok(accountService.block(accountNumber, coreUserId));
    }

    @PatchMapping("/{accountNumber}/suspend")
    public ResponseEntity<AccountResponseDTO> suspend(
            @PathVariable String accountNumber,
            @RequestHeader(CORE_USER_HEADER) Integer coreUserId) {
        return ResponseEntity.ok(accountService.suspend(accountNumber, coreUserId));
    }

    @PostMapping("/{accountNumber}/credit")
    public ResponseEntity<TransactionResponseDTO> credit(@PathVariable String accountNumber,
                                                         @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.credit(accountNumber, request.amount()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDTO> transfer(@RequestBody TransferRequest request) {
        return ResponseEntity.ok(accountService.transfer(request.origin(), request.destination(), request.amount(), request.uuid()));
    }

    @GetMapping("/default/favorite")
    public ResponseEntity<AccountResponseDTO> getFavoriteAccount() {
        return ResponseEntity.ok(accountService.getFavoriteAccount());
    }

    record AmountRequest(BigDecimal amount) {}

    record TransferRequest(String origin, String destination, BigDecimal amount, String uuid) {}
}
