package pl.prokodzik.altmetrik.payments.exceptions;

public class PaymentAlreadyExistsException extends Exception {
    public PaymentAlreadyExistsException(String s) {
        super(s);
    }
}
