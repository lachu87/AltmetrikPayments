package pl.prokodzik.altmetrik.payments.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import pl.prokodzik.altmetrik.payments.exceptions.DataCorruptionException;

import java.util.Currency;
import java.util.Locale;

class CsvMapperTest {

    private CsvMapper csvMapper;

    @BeforeEach
    void setUp() {
        csvMapper = new CsvMapper();
    }

    @Test
    void should_write_to_csv() {
        //given:
        StorablePayment storablePayment = new StorablePayment("123", 10L, Currency.getInstance(Locale.US), "1", "1");
        String[] expectedCsv = {"123","10","USD","1","1"};

        //when:
        String[] actualCsv = csvMapper.toCsv(storablePayment);

        //then:
        Assertions.assertArrayEquals(expectedCsv, actualCsv);
    }

    @Test
    void should_read_from_csv() throws DataCorruptionException {
        //given:
        String[] providedCsv = {"123","10","USD","1","1"};
        StorablePayment expectedStorablePayment = new StorablePayment("123", 10L, Currency.getInstance(Locale.US), "1", "1");

        //when:
        StorablePayment actualStorablePayment = csvMapper.fromCsv(providedCsv);

        //then:
        Assertions.assertEquals(expectedStorablePayment, actualStorablePayment);
    }

    @Test
    void should_throw_when_invalid_data_from_csv() {
        //given:
        String[] providedCsv = {"123","10","USD","1"};
        StorablePayment expectedStorablePayment = new StorablePayment("123", 10L, Currency.getInstance(Locale.US), "1", "1");

        //when:
        Executable executable = () -> csvMapper.validateLine(providedCsv);

        //then:
        Assertions.assertThrows(DataCorruptionException.class, executable);
    }

}