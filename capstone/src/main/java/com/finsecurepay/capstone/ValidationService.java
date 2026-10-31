
package com.finsecurepay.capstone;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000.00");

    private static final Set<String> CURRENCIES = Set.of("SGD", "USD", "EUR");

    public ValidationResult validate(Transaction transaction) {

        List<String> reasons = new ArrayList<>();

        if (transaction == null) {
            return new ValidationResult(false, "REJECTED", List.of("Transaction is required"));
        }

        if (transaction.transactionId() == null || transaction.transactionId().isBlank()) {
            reasons.add("Transaction ID not present");
        }

        if (transaction.amount() == null || transaction.amount().compareTo(BigDecimal.ZERO) <= 0) {
            reasons.add("Amount must be greater than 0");
        } else if (transaction.amount().compareTo(MAX_AMOUNT) > 0) {
            reasons.add("Amount exceeds maximum of 100,000");
        }

        if (transaction.currency() == null || !CURRENCIES.contains(transaction.currency())) {
            reasons.add("Unsupported currency");
        }

        boolean sourceAccountMissing = transaction.sourceAccount() == null || transaction.sourceAccount().isBlank();
        if (sourceAccountMissing) {
            reasons.add("Source account is required");
        }

        boolean destAccountMissing = transaction.destinationAccount() == null || transaction.destinationAccount().isBlank();
        if (destAccountMissing) {
            reasons.add("Destination account is required");
        }

        if (!sourceAccountMissing && !destAccountMissing && transaction.sourceAccount().equals(transaction.destinationAccount())) {
            reasons.add("Source and destination must differ");
        }

        boolean valid = reasons.isEmpty();
        return new ValidationResult(valid, valid ? "ACCEPTED" : "REJECTED", reasons);
    }
}
