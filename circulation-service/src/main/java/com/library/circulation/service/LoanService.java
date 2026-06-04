package com.library.circulation.service;

import com.library.circulation.client.CatalogServiceClient;
import com.library.circulation.client.CopySummary;
import com.library.circulation.client.UserServiceClient;
import com.library.circulation.client.UserSummary;
import com.library.circulation.dto.CheckoutRequest;
import com.library.circulation.entity.Loan;
import com.library.circulation.entity.LoanStatus;
import com.library.circulation.exception.CheckoutValidationException;
import com.library.circulation.exception.InvalidLoanStateException;
import com.library.circulation.exception.LoanNotFoundException;
import com.library.circulation.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LoanService {
    private static final Logger log = (Logger) LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final UserServiceClient userServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final int loanDurationDays;

    public LoanService(LoanRepository loanRepository,
                       UserServiceClient userServiceClient,
                       CatalogServiceClient catalogServiceClient,
                       @Value("${library.loan.duration-days:14}") int loanDurationDays) {
        this.loanRepository = loanRepository;
        this.userServiceClient = userServiceClient;
        this.catalogServiceClient = catalogServiceClient;
        this.loanDurationDays = loanDurationDays;

    }

    public Loan checkout(CheckoutRequest request) {
        log.info("Checkout request: user={}, copy={}", request.userId(), request.itemCopyId());

        UserSummary user = userServiceClient.getUser(request.userId());
        if(!"ACTIVE".equals(user.status())) {
            throw new CheckoutValidationException(
                    "User " + request.userId() + " is not active (Status: " + user.status() + ")"
            );
        }

        CopySummary copy = catalogServiceClient.getCopy(request.itemCopyId());
        if(!"AVAILABLE".equals(copy.status())) {
            throw new CheckoutValidationException(
                    "Copy " + request.itemCopyId() + " is not active (Status: " + copy.status() + ")"
            );
        }

        if (loanRepository.existsByItemCopyIdAndStatus(request.itemCopyId(), LoanStatus.ACTIVE)) {
            throw new CheckoutValidationException(
                    "Copy " + request.itemCopyId() + " already has an active loan in circulation"
            );
        }

        //Creating Loan

        Instant now = Instant.now();
        Loan loan = new Loan();
        loan.setUserId(request.userId());
        loan.setItemCopyId(request.itemCopyId());
        loan.setCheckoutDate(now);
        loan.setDueDate(now.plus(Duration.ofDays(loanDurationDays)));
        loan.setLoanStatus(LoanStatus.ACTIVE);
        Loan saved = loanRepository.save(loan);

        //Communicate that the copy is checked out to Catalog Service

        catalogServiceClient.updateCopyStatus(request.itemCopyId(), "CHECKED_OUT");

        log.info("Loan created: id{}, dueDate{}", saved.getId(), saved.getDueDate());
        return saved;
    }

    public Loan returnLoan(UUID loanId) {
        Loan loan = findById(loanId);

        if (loan.getLoanStatus() == LoanStatus.RETURNED) {
            throw new InvalidLoanStateException(
                    "Loan " + loanId + " has already been returned"
            );
        }

        loan.setReturnDate(Instant.now());
        loan.setLoanStatus(LoanStatus.RETURNED);

        //Communicate that copy is available to Catalog Service

        try {
            catalogServiceClient.updateCopyStatus(loan.getItemCopyId(), "AVAILABLE");
        } catch (Exception e) {
            log.warn("Failed to update copy status to AVAILABLE for copy {}; will reconcile later",
                    loan.getItemCopyId(), e);
        }

        log.info("Loan returned: id= {}", loanId);

        return loan;
    }

    //Read Methods

    @Transactional(readOnly = true)
    public Loan findById(UUID id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Loan> findAll(UUID userId, LoanStatus status, boolean overdueOnly, Pageable pageable) {
        if(overdueOnly) {
            return loanRepository.findCurrentlyOverdue(LoanStatus.ACTIVE, Instant.now(), pageable);
    }

        if(userId != null && status != null) {
            return loanRepository.findByUserIdAndStatus(userId, status, pageable);
    }
        if(userId != null) {
            return loanRepository.findByUserId(userId, pageable);
        }
        if(status != null) {
            return loanRepository.findByStatus(status, pageable);
        }
        return loanRepository.findAll(pageable);
}

    @Transactional(readOnly = true)
    public List<Loan> findActiveLoansForUser(UUID userId) {
        return loanRepository.findByUserIdAndStatusOrderByDueDateAsc(userId, LoanStatus.ACTIVE);
    }

}
