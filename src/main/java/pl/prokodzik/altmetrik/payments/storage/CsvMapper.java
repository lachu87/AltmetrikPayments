package pl.prokodzik.altmetrik.payments.storage;

import pl.prokodzik.altmetrik.payments.exceptions.DataCorruptionException;

import java.util.Arrays;
import java.util.Currency;

public class CsvMapper {

    public String[] toCsv(StorablePayment storablePayment) {
        String[] strings = new String[5];
        strings[0] = storablePayment.getId();
        strings[1] = storablePayment.getAmount().toString();
        strings[2] = storablePayment.getCurrency().getCurrencyCode();
        strings[3] = storablePayment.getUserId();
        strings[4] = storablePayment.getAccountNumber();
        return strings;
    }

    public StorablePayment fromCsv(String[] csvRecord) {
        return StorablePayment.builder()
                .id(csvRecord[0])
                .amount(Long.valueOf(csvRecord[1]))
                .currency(Currency.getInstance(csvRecord[2]))
                .userId(csvRecord[3])
                .accountNumber(csvRecord[4]).build();
    }

    public void validateLine(String[] csvRecord) throws DataCorruptionException {
        if (csvRecord.length != 5) {
            throw new DataCorruptionException("Data in row: " + Arrays.toString(csvRecord) + " corrupted!!!");
        }
    }
}
