package pl.prokodzik.altmetrik.payments.service;

import pl.prokodzik.altmetrik.payments.exceptions.InputDataException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentAlreadyExistsException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentNotFoundException;
import pl.prokodzik.altmetrik.payments.storage.Repository;
import pl.prokodzik.altmetrik.payments.storage.StorablePayment;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

public class SimplePaymentsService implements PaymentsService<PaymentInput, StorablePayment, String> {

    private final Repository<StorablePayment, String> repository;

    public SimplePaymentsService(final Repository<StorablePayment, String> repository) {
        this.repository = repository;
    }

    @Override
    public StorablePayment getById(final String id) throws PaymentNotFoundException {
        return repository.getById(id).orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
    }

    @Override
    public List<StorablePayment> getAll() {
        return repository.getAll();
    }

    @Override
    public StorablePayment save(final PaymentInput paymentInput) throws PaymentAlreadyExistsException {
        StorablePayment storablePayment = StorablePayment.builder()
                .userId(paymentInput.getUserId())
                .currency(Currency.getInstance(paymentInput.getCurrency()))
                .accountNumber(paymentInput.getAccountNumber())
                .amount(paymentInput.getAmount()).build();
        return repository.save(storablePayment);
    }

    @Override
    public StorablePayment update(final PaymentInput paymentInput, final String id) throws InputDataException, PaymentNotFoundException {
        if (id == null || id.isEmpty()) {
            throw new InputDataException("Update process exception: Id not provided");
        }
        Optional<StorablePayment> entityForUpdateOptional = repository.getById(id);
        if (entityForUpdateOptional.isEmpty()) {
            throw new PaymentNotFoundException("Update process exception: No payment for given id: " + id);
        }
        StorablePayment toBeUpdated = entityForUpdateOptional.get();

        StorablePayment storablePayment = StorablePayment.builder()
                .id(id)
                .userId(Optional.ofNullable(paymentInput.getUserId()).orElse(toBeUpdated.getUserId()))
                .currency(Optional.ofNullable(paymentInput.getCurrency()).map(Currency::getInstance).orElse(toBeUpdated.getCurrency()))
                .accountNumber(Optional.ofNullable(paymentInput.getAccountNumber()).orElse(toBeUpdated.getAccountNumber()))
                .amount(Optional.ofNullable(paymentInput.getAmount()).orElse(toBeUpdated.getAmount())).build();

        return repository.update(storablePayment);
    }

    @Override
    public void delete(final String id) throws PaymentNotFoundException {
        repository.delete(id);
    }

}
