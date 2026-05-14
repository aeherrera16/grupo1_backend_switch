package com.banquito.core.service;

import com.banquito.core.dto.BranchRequestDTO;
import com.banquito.core.dto.BranchResponseDTO;

import java.util.List;

public interface IBranchService {

    List<BranchResponseDTO> findAll();

    BranchResponseDTO findByCode(String code);

    BranchResponseDTO create(BranchRequestDTO request);
}
