package pl.prokodzik.altmetrik.payments.storage;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentAlreadyExistsException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentNotFoundException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

class CsvRepositoryTest {

    private CsvRepository csvRepository;

    private static final CsvMapper csvMapper = new CsvMapper();
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        Supplier<String> idSupplier = () -> UUID.randomUUID().toString();

        tempFile = File.createTempFile("csvRepositoryTest", "csv");

        csvRepository = new CsvRepository(idSupplier, csvMapper, tempFile);
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void should_store_to_file() throws PaymentAlreadyExistsException, IOException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");

        //when:
        csvRepository.save(storablePayment);

        //then:
        Assertions.assertEquals(1, getFileLines(tempFile).count());
    }

    @Test
    void should_throw_when_storing_twice() throws PaymentAlreadyExistsException, IOException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");

        //when:
        csvRepository.save(storablePayment);
        Executable executable = () -> csvRepository.save(storablePayment);

        //then:
        Assertions.assertEquals(1, getFileLines(tempFile).count());
        Assertions.assertThrows(PaymentAlreadyExistsException.class, executable);
    }

    @Test
    void should_update_data_in_file() throws PaymentAlreadyExistsException, IOException, PaymentNotFoundException, CsvValidationException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");
        StorablePayment storablePayment2 = new StorablePayment(UUID.randomUUID().toString(), 11L, Currency.getInstance(Locale.US), "1", "1");
        StorablePayment saved = csvRepository.save(storablePayment);
        StorablePayment saved2 = csvRepository.save(storablePayment2);
        StorablePayment build = StorablePayment.builder()
                .id(saved2.getId())
                .accountNumber("2")
                .userId(saved2.getUserId())
                .amount(77L)
                .currency(saved2.getCurrency()).build();
        //when:
        csvRepository.update(build);

        //then:
        Assertions.assertEquals(2, getFileLines(tempFile).count());
        Assertions.assertTrue(fileContainsEntry(build, tempFile).isPresent());
        Assertions.assertFalse(fileContainsEntry(saved2, tempFile).isPresent());
    }

    @Test
    void should_delete_data_from_file() throws PaymentAlreadyExistsException, IOException, PaymentNotFoundException {
        //given:
        StorablePayment storablePayment = new StorablePayment(UUID.randomUUID().toString(), 10L, Currency.getInstance(Locale.CANADA), "1", "1");
        StorablePayment save = csvRepository.save(storablePayment);

        //when:
        csvRepository.delete(save.getId());

        //then:
        Assertions.assertEquals(0, getFileLines(tempFile).count());
    }


    private Stream<String> getFileLines(final File file) throws IOException {
        return Files.lines(Paths.get(file.getAbsolutePath()));
    }

    private Optional<String> fileContainsEntry(final StorablePayment storablePayment, final File file) throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new FileReader(file));
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if (storablePayment.getId().equals(nextLine[0]) && storablePayment.getAmount().toString().equals(nextLine[1])) {
                return Arrays.stream(nextLine).reduce((str1, str2) -> str1 + str2);
            }
        }
        reader.close();
        return Optional.empty();
    }

}