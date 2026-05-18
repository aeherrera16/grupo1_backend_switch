package ec.edu.espe.banquito.switchpagos.repository;

import ec.edu.espe.banquito.switchpagos.model.ServiceFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ServiceFeeRuleRepository extends JpaRepository<ServiceFeeRule, Integer> {

    @Query("SELECT r FROM ServiceFeeRule r WHERE r.serviceType = 'PAGOS_MASIVOS' " +
            "AND r.feeType = 'UNIT_FEE' " +
            "AND :txCount >= r.minAmount " +
            "AND (:txCount <= r.maxAmount OR r.maxAmount IS NULL)")
    Optional<ServiceFeeRule> findRuleByTransactionCount(@Param("txCount") BigDecimal txCount);

    @Query("""
        SELECT r
        FROM ServiceFeeRule r
        WHERE r.serviceType = 'PAGOS_MASIVOS'
        AND r.feeType = 'UNIT_FEE'
        AND :transactions >= r.minSuccessfulTransactions
        AND (
            :transactions <= r.maxSuccessfulTransactions
            OR r.maxSuccessfulTransactions IS NULL
        )
        ORDER BY r.minSuccessfulTransactions DESC
    """)
    Optional<ServiceFeeRule> findRule(
            @Param("transactions") Integer transactions
    );

}
