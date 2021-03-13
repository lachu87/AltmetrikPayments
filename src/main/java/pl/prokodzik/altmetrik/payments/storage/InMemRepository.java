package pl.prokodzik.altmetrik.payments.storage;

import pl.prokodzik.altmetrik.payments.exceptions.PaymentAlreadyExistsException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class InMemRepository extends Repository<StorablePayment, String> {

    private final Map<String, StorablePayment> payments;

    protected InMemRepository(final Supplier<String> idSupplier, final Map<String, StorablePayment> paymentMap) {
        super(idSupplier);
        payments = paymentMap;
    }

    public InMemRepository(final Supplier<String> idSupplier) {
        super(idSupplier);
        payments = new HashMap<>();
    }

    public void cleanup() {
        payments.clear();
    }

    @Override
    public StorablePayment save(final StorablePayment storablePayment) throws PaymentAlreadyExistsException {
        String uniqueId = storablePayment.getId();
        if (uniqueId == null) {
            uniqueId = this.supplyUniqueId();
        }
        if (payments.containsKey(uniqueId)) {
            throw new PaymentAlreadyExistsException("Payment already exists");
        }
        StorablePayment build = StorablePayment.builder().accountNumber(storablePayment.getAccountNumber())
                .amount(storablePayment.getAmount()).currency(storablePayment.getCurrency())
                .userId(storablePayment.getUserId()).id(uniqueId).build();
        payments.put(uniqueId, build);
        return payments.get(uniqueId);
    }

    @Override
    public StorablePayment update(final StorablePayment storablePayment) throws PaymentNotFoundException {
        String storablePaymentId = storablePayment.getId();
        if (!payments.containsKey(storablePaymentId)) {
            throw new PaymentNotFoundException("Payment not found");
        }
        payments.put(storablePaymentId, storablePayment);
        return payments.get(storablePaymentId);
    }

    @Override
    public void delete(String id) throws PaymentNotFoundException {
        if (!payments.containsKey(id)) {
            throw new PaymentNotFoundException("Payment not found");
        }
        payments.remove(id);
    }

    @Override
    public Optional<StorablePayment> getById(String id) {
        return Optional.ofNullable(payments.get(id));
    }

    @Override
    public List<StorablePayment> getAll() {
        return new ArrayList<>(payments.values());
    }
}
