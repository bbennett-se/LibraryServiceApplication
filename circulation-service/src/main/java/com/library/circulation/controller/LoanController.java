package com.library.circulation.controller;


import com.library.circulation.dto.CheckoutRequest;
import com.library.circulation.dto.LoanResponse;
import com.library.circulation.entity.Loan;
import com.library.circulation.entity.LoanStatus;
import com.library.circulation.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/circulation")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<LoanResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        Loan loan = loanService.checkout(request);
        return ResponseEntity
                .created(URI.create("/api/circulation/loans/" + loan.getId()))
                .body(LoanResponse.from(loan));
    }

    @PostMapping("/returns/{loanId}")
    public LoanResponse returnLoan(@PathVariable UUID loanId){
        return LoanResponse.from(loanService.returnLoan(loanId));
    }

    @GetMapping("/loans/{id}")
    public LoanResponse getById(@PathVariable UUID id) {
        return LoanResponse.from(loanService.findById(id));

    }

    @GetMapping("/loans")
    public Page<LoanResponse> getAll(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false, defaultValue = "false") boolean overdue,
            Pageable pageable
            ) {
        return loanService.findAll(userId, status, overdue, pageable)
                .map(LoanResponse::from);
    }

    @GetMapping("/user/{userId}/active-loans")
    public List<LoanResponse> getActiveLoansForUser(@PathVariable UUID userId) {
        return loanService.findActiveLoansForUser(userId)
                .stream()
                .map(LoanResponse::from)
                .toList();
    }
}
