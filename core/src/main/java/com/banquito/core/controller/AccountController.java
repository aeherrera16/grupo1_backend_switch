package com.banquito.core.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banquito.core.dto.AccountRequestDTO;
import com.banquito.core.dto.AccountResponseDTO;
import com.banquito.core.dto.TransactionResponseDTO;
import com.banquito.core.service.IAccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/core/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final IAccountService accountService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponseDTO>> findByCustomerId(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(accountService.findByCustomerId(customerId, 1));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> findByAccountNumber(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.findByAccountNumber(accountNumber, 1));
    }

    @GetMapping("/customer/{customerId}/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> findTransactionsByCustomerId(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(accountService.findTransactionsByCustomerId(customerId, 1));
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> create(
            @RequestBody AccountRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(request, 1));
    }

    @PatchMapping("/{accountNumber}/inactivate")
    public ResponseEntity<AccountResponseDTO> inactivate(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.inactivate(accountNumber, 1));
    }

    @PatchMapping("/{accountNumber}/block")
    public ResponseEntity<AccountResponseDTO> block(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.block(accountNumber, 1));
    }

    @PatchMapping("/{accountNumber}/suspend")
    public ResponseEntity<AccountResponseDTO> suspend(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.suspend(accountNumber, 1));
    }

    @PatchMapping("/{accountNumber}/activate")
    public ResponseEntity<AccountResponseDTO> activate(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.activate(accountNumber, 1));
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

    @GetMapping("/favorite/customer/{customerId}")
    public ResponseEntity<AccountResponseDTO> getFavorite(@PathVariable Integer customerId) {
        return ResponseEntity.ok(accountService.getFavoriteAccount(customerId));
    }

    @PutMapping("/{accountNumber}/favorite/customer/{customerId}")
    public ResponseEntity<AccountResponseDTO> updateFavorite(
            @PathVariable String accountNumber,
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(accountService.updateFavoriteAccount(accountNumber, customerId));
    }

    record AmountRequest(BigDecimal amount) {}

    record TransferRequest(String origin, String destination, BigDecimal amount, String uuid) {}
}
