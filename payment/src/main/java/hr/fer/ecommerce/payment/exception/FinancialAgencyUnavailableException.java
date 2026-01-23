package hr.fer.ecommerce.payment.exception;

public class FinancialAgencyUnavailableException extends RuntimeException {
    public FinancialAgencyUnavailableException() {
        super("FINA service is currently unavailable");
    }

    public FinancialAgencyUnavailableException(String reason) {
        super(String.format("FINA service is currently unavailable: %s", reason));
    }
}
