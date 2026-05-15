package com.banquito.core.repository;

import com.banquito.core.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {

    Optional<Branch> findByBranchCode(String branchCode);
}