package com.banquito.core.controller;

import com.banquito.core.dto.BranchRequestDTO;
import com.banquito.core.dto.BranchResponseDTO;
import com.banquito.core.service.IBranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/core/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final IBranchService branchService;

    @GetMapping
    public ResponseEntity<List<BranchResponseDTO>> findAll() {
        return ResponseEntity.ok(branchService.findAll());
    }

    @GetMapping("/{code}")
    public ResponseEntity<BranchResponseDTO> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(branchService.findByCode(code));
    }

    @PostMapping
    public ResponseEntity<BranchResponseDTO> create(@RequestBody BranchRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.create(request));
    }
}
