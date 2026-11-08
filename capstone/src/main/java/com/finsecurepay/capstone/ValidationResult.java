
package com.finsecurepay.capstone;

import java.util.List;

public record ValidationResult(
    boolean valid,
    String status,
    List<String> reasons
) {
    public ValidationResult {
        reasons = List.copyOf(reasons);
    }
}
