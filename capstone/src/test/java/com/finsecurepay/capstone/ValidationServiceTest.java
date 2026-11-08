
package com.finsecurepay.capstone;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    private final ValidationService service = new ValidationService();

    private Transaction createTransaction(
        String id, String amount, String currency, String source, String destination
    ) {
        return new Transaction(id, new BigDecimal(amount), currency, source, destination);
    }

    @Test
    void testAcceptValidTransaction() {
        Transaction tx = createTransaction("TX-1001", "150.50", "SGD", "1001", "1002");

        ValidationResult result = service.validate(tx);

        assertTrue(result.valid());
        assertEquals("REJECTED", result.status());
        assertTrue(result.reasons().isEmpty());
    }

    @Test
    void testRejectNegativeAmount() {
        Transaction tx = createTransaction("TX-1002", "-150.50", "SGD", "1001", "1002");

        ValidationResult result = service.validate(tx);

        assertFalse(result.valid());
        assertEquals("REJECTED", result.status());
        assertTrue(result.reasons().contains("Amount must be greater than 0"));
    }

    @Test
    void testRejectZeroAmount() {
        Transaction tx = createTransaction("TX-1003", "0", "SGD", "1001", "1002");

        ValidationResult result = service.validate(tx);

        assertFalse(result.valid());
        assertEquals("REJECTED", result.status());
        assertTrue(result.reasons().contains("Amount must be greater than 0"));
    }

    @Test
    void testRejectLargeAmount() {
        Transaction tx = createTransaction("TX-1004", "100001", "SGD", "1001", "1002");

        ValidationResult result = service.validate(tx);

        assertFalse(result.valid());
        assertEquals("REJECTED", result.status());
        assertTrue(result.reasons().contains("Amount exceeds maximum of 100,000"));
    }

    @Test
    void testRejectInvalidCurrency() {
        Transaction tx = createTransaction("TX-1005", "150.50", "XYZ", "1001", "1002");

        ValidationResult result = service.validate(tx);

        assertFalse(result.valid());
        assertEquals("REJECTED", result.status());
        assertTrue(result.reasons().contains("Unsupported currency"));
    }

    @Test
    void testRejectSameOriginDest() {
        Transaction tx = createTransaction("TX-1006", "150.50", "SGD", "1001", "1001");

        ValidationResult result = service.validate(tx);

        assertFalse(result.valid());
        assertEquals("REJECTED", result.status());
        assertTrue(result.reasons().contains("Source and destination must differ"));
    }

    @Test
    void testRejectBlankTID() {
        Transaction tx = createTransaction("", "150.50", "SGD", "1001", "1002");

        ValidationResult result = service.validate(tx);

        assertFalse(result.valid());
        assertEquals("REJECTED", result.status());
        assertTrue(result.reasons().contains("Transaction ID not present"));
    }

    @Test
    void testRejectBlankSource() {
        Transaction tx = createTransaction("TX-1007", "150.50", "SGD", "", "1002");

        ValidationResult result = service.validate(tx);

        assertFalse(result.valid());
        assertEquals("REJECTED", result.status());
        assertTrue(result.reasons().contains("Source account is required"));
    }

    @Test
    void testRejectBlankDest() {
        Transaction tx = createTransaction("TX-1008", "150.50", "SGD", "1001", "");

        ValidationResult result = service.validate(tx);

        assertFalse(result.valid());
        assertEquals("REJECTED", result.status());
        assertTrue(result.reasons().contains("Destination account is required"));
    }

    @Test
    void testAbleToHandleMultipleReasons() {
        Transaction tx = createTransaction("TX-1009", "-150.50", "SGD", "1001", "1001"
        );

        ValidationResult result = service.validate(tx);

        assertFalse(result.valid());
        assertEquals("REJECTED", result.status());
        assertEquals(2, result.reasons().size());
    }

    @Test
    void testRejectNullTransaction() {
        ValidationResult result = service.validate(null);

        assertFalse(result.valid());
        assertEquals("REJECTED", result.status());
        assertTrue(result.reasons().contains("Transaction is required"));
    }
}
