package pl.prokodzik.altmetrik.payments.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentAlreadyExistsException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentNotFoundException;

import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

class InMemRepositoryTest {

    private InMemRepository inMemRepository;
    private HashMap<String, StorablePayment> paymentMap;
    private static final Supplier<String> idSupplier = () -> UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        paymentMap = new HashMap<>();
        inMemRepository = new InMemRepository(idSupplier, paymentMap);
    }

    @Test
    void should_save() throws PaymentAlreadyExistsException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");

        //when:
        inMemRepository.save(storablePayment);

        //then:
        Assertions.assertEquals(1, paymentMap.size());
    }

    @Test
    void should_throw_when_save_twice() throws PaymentAlreadyExistsException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");

        //when:
        inMemRepository.save(storablePayment);
        Executable executable = () -> inMemRepository.save(storablePayment);

        //then:
        Assertions.assertEquals(1, paymentMap.size());
        Assertions.assertThrows(PaymentAlreadyExistsException.class, executable);
    }

    @Test
    void update() throws PaymentAlreadyExistsException, PaymentNotFoundException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");
        inMemRepository.save(storablePayment);
        StorablePayment paymentUpdate = new StorablePayment(storablePayment.getId(), 12L, Currency.getInstance(Locale.US), "2", "4");

        //when:
        inMemRepository.update(paymentUpdate);

        //then:
        Assertions.assertEquals(1, paymentMap.size());
        Assertions.assertTrue(paymentMap.containsKey(paymentUpdate.getId()));
        Assertions.assertTrue(paymentMap.containsValue(paymentUpdate));
    }

    @Test
    void delete() throws PaymentAlreadyExistsException, PaymentNotFoundException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");
        inMemRepository.save(storablePayment);

        //when:
        inMemRepository.delete(storablePayment.getId());

        //then:
        Assertions.assertTrue(paymentMap.isEmpty());
    }

    @Test
    void getById() throws PaymentAlreadyExistsException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");
        inMemRepository.save(storablePayment);

        //when:
        Optional<StorablePayment> byId = inMemRepository.getById(storablePayment.getId());

        //then:
        Assertions.assertTrue(byId.isPresent());
        Assertions.assertEquals(storablePayment.getId(), byId.get().getId());
    }

    @Test
    void getAll() throws PaymentAlreadyExistsException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");
        inMemRepository.save(storablePayment);
        StorablePayment storablePayment2 = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");
        inMemRepository.save(storablePayment2);

        //when:
        List<StorablePayment> all = inMemRepository.getAll();

        //then:
        Assertions.assertEquals(2, all.size());
    }
}