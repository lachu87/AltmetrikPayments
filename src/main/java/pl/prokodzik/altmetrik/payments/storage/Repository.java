package pl.prokodzik.altmetrik.payments.storage;

import pl.prokodzik.altmetrik.payments.exceptions.PaymentAlreadyExistsException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class Repository<S extends StorablePayment, K> {

    private final Supplier<K> idSupplier;

    protected Repository(Supplier<K> idSupplier) {
        this.idSupplier = idSupplier;
    }

    public K supplyUniqueId() {
        return idSupplier.get();
    }

    public abstract S save(S storablePayment) throws PaymentAlreadyExistsException;

    public abstract S update(S storablePayment) throws PaymentNotFoundException;

    public abstract void delete(K id) throws PaymentNotFoundException;

    public abstract Optional<S> getById(K id);

    public abstract List<S> getAll();
}
