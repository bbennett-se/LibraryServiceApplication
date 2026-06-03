package com.library.circulation.service;

import com.library.circulation.client.CatalogServiceClient;
import com.library.circulation.client.CopySummary;
import com.library.circulation.client.UserServiceClient;
import com.library.circulation.client.UserSummary;
import com.library.circulation.dto.CheckoutRequest;
import com.library.circulation.entity.Loan;
import com.library.circulation.entity.LoanStatus;
import com.library.circulation.exception.CheckoutValidationException;
import com.library.circulation.repository.LoanRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

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
        //TODO: FIX LOG
        // log.info("Checkout request: user={}, copy={}", request.userId(), request.itemCopyId());

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
    }

}
