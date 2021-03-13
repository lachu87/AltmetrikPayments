package pl.prokodzik.altmetrik.payments.service;

import pl.prokodzik.altmetrik.payments.exceptions.InputDataException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentAlreadyExistsException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentNotFoundException;
import pl.prokodzik.altmetrik.payments.storage.StorablePayment;

import java.util.List;

public interface PaymentsService<I extends PaymentInput, S extends StorablePayment, K> {

    S getById(final K id) throws PaymentNotFoundException;

    List<S> getAll();

    S save(final I paymentInput) throws PaymentAlreadyExistsException;

    S update(final I paymentInput, final K id) throws InputDataException, PaymentNotFoundException;

    void delete(final K id) throws PaymentNotFoundException;
}
