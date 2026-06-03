package com.library.circulation.repository;

import com.library.circulation.entity.Loan;
import com.library.circulation.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    Page<Loan> findByUserId(UUID userID, Pageable pageable);

    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    Page<Loan> findByUserIdAndStatus(UUID id, LoanStatus status, Pageable pageable);

    List<Loan> findByUserIdAndStatusOrderByDueDateAsc(UUID id, LoanStatus status);

    boolean existsByItemCopyIdAndStatus(UUID copyId, LoanStatus status);

    @Query("""
           SELECT l FROM Loan l
           WHERE l.status = :status
             AND l.dueDate < :now
           """)
    Page<Loan> findCurrentlyOverdue(@Param("status") LoanStatus status,
                                    @Param("now") Instant now,
                                    Pageable pageable);

}
