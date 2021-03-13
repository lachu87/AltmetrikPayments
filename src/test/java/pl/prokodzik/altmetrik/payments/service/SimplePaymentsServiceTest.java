package pl.prokodzik.altmetrik.payments.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import pl.prokodzik.altmetrik.payments.exceptions.InputDataException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentAlreadyExistsException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentNotFoundException;
import pl.prokodzik.altmetrik.payments.storage.InMemRepository;
import pl.prokodzik.altmetrik.payments.storage.StorablePayment;

import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

class SimplePaymentsServiceTest {

    private InMemRepository inMemRepository;
    private PaymentsService<PaymentInput, StorablePayment, String> paymentsService;
    private static final Supplier<String> idSupplier = () -> UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        inMemRepository = new InMemRepository(idSupplier);
        paymentsService = new SimplePaymentsService(inMemRepository);
    }

    @AfterEach
    void tearDown() {
        inMemRepository.cleanup();
    }

    @Test
    void should_get_by_id() throws PaymentAlreadyExistsException, PaymentNotFoundException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");
        inMemRepository.save(storablePayment);

        //when:
        StorablePayment byId = paymentsService.getById(storablePayment.getId());

        //then:
        Assertions.assertEquals(storablePayment, byId);
    }

    @Test
    void should_throw_when_entry_not_found() throws PaymentAlreadyExistsException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");
        inMemRepository.save(storablePayment);

        //when:
        Executable runnable = () -> paymentsService.getById(UUID.randomUUID().toString());

        //then:
        Assertions.assertThrows(PaymentNotFoundException.class, runnable);
    }

    @Test
    void should_get_all() throws PaymentAlreadyExistsException {
        //given:
        StorablePayment.StorablePaymentBuilder storablePaymentBuilder = StorablePayment.builder().id(UUID.randomUUID().toString())
                .amount(10L).currency(
                        Currency.getInstance(Locale.CANADA)).accountNumber("1").userId("1");
        StorablePayment storablePayment = storablePaymentBuilder.build();
        StorablePayment storablePayment1 = storablePaymentBuilder.id(UUID.randomUUID().toString()).amount(12L).userId("2").accountNumber("3").build();

        inMemRepository.save(storablePayment);
        inMemRepository.save(storablePayment1);

        //when:
        List<StorablePayment> all = inMemRepository.getAll();

        //then:
        Assertions.assertFalse(all.isEmpty());
        Assertions.assertEquals(2, all.size());

    }

    @Test
    void should_create() throws PaymentAlreadyExistsException {
        //given:
        PaymentInput paymentInput = new PaymentInput(10L, "USD", "1", "123");

        //when:
        StorablePayment byId = paymentsService.save(paymentInput);

        //then:
        Optional<StorablePayment> byId1 = inMemRepository.getById(byId.getId());
        Assertions.assertTrue(byId1.isPresent());

    }

    @Test
    void should_update() throws PaymentAlreadyExistsException, InputDataException, PaymentNotFoundException {
        //given:
        StorablePayment build = StorablePayment.builder().id(UUID.randomUUID().toString())
                .amount(10L).currency(
                        Currency.getInstance(Locale.US)).accountNumber("1").userId("1").build();

        StorablePayment oldPaymentSaved = inMemRepository.save(build);
        PaymentInput paymentInput = new PaymentInput(12L, "USD", "1", "123");


        //when:
        paymentsService.update(paymentInput, oldPaymentSaved.getId());
        Optional<StorablePayment> byId = inMemRepository.getById(oldPaymentSaved.getId());

        //then:
        Assertions.assertTrue(byId.isPresent());
        StorablePayment updatedPayment = byId.get();
        Assertions.assertEquals(oldPaymentSaved.getId(), updatedPayment.getId());
        Assertions.assertNotEquals(oldPaymentSaved.getAccountNumber(), updatedPayment.getAccountNumber());
        Assertions.assertEquals(updatedPayment.getAccountNumber(), paymentInput.getAccountNumber());
        Assertions.assertNotEquals(oldPaymentSaved.getAmount(), updatedPayment.getAmount());
        Assertions.assertEquals(updatedPayment.getAmount(), paymentInput.getAmount());
    }

    @Test
    void should_throw_when_updating_non_existing_entry() throws PaymentAlreadyExistsException {
        //given:
        StorablePayment build = StorablePayment.builder().id(UUID.randomUUID().toString())
                .amount(10L).currency(
                        Currency.getInstance(Locale.US)).accountNumber("1").userId("1").build();

        inMemRepository.save(build);
        PaymentInput paymentInput = new PaymentInput(12L, "USD", "1", "123");

        //when:
        Executable executable = () -> paymentsService.update(paymentInput, UUID.randomUUID().toString());

        //then:
        Assertions.assertThrows(PaymentNotFoundException.class, executable);
    }

    @Test
    void should_throw_when_entry_does_not_have_id() throws PaymentAlreadyExistsException {
        //given:
        StorablePayment build = StorablePayment.builder().id(UUID.randomUUID().toString())
                .amount(10L).currency(
                        Currency.getInstance(Locale.US)).accountNumber("1").userId("1").build();

        inMemRepository.save(build);
        PaymentInput paymentInput = new PaymentInput(12L, "USD", "1", "123");

        //when:
        Executable executable = () -> paymentsService.update(paymentInput, null);

        //then:
        Assertions.assertThrows(InputDataException.class, executable);
    }

    @Test
    void should_delete() throws PaymentAlreadyExistsException, PaymentNotFoundException {
        //given:
        StorablePayment build = StorablePayment.builder().id(UUID.randomUUID().toString())
                .amount(10L).currency(
                        Currency.getInstance(Locale.US)).accountNumber("1").userId("1").build();

        StorablePayment oldPaymentSaved = inMemRepository.save(build);

        //when:
        paymentsService.delete(oldPaymentSaved.getId());
        Optional<StorablePayment> byId = inMemRepository.getById(oldPaymentSaved.getId());

        //then:
        Assertions.assertTrue(byId.isEmpty());
    }

    @Test
    void should_throw_when_deleting_unknown_key() throws PaymentAlreadyExistsException {
        //given:
        StorablePayment build = StorablePayment.builder().id(UUID.randomUUID().toString())
                .amount(10L).currency(
                        Currency.getInstance(Locale.US)).accountNumber("1").userId("1").build();

        inMemRepository.save(build);

        //when:
        Executable runnable = () -> paymentsService.delete(UUID.randomUUID().toString());

        //then:
        Assertions.assertThrows(PaymentNotFoundException.class, runnable);
    }

}