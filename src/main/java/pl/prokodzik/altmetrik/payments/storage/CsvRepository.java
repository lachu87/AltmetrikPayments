package pl.prokodzik.altmetrik.payments.storage;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.prokodzik.altmetrik.payments.exceptions.DataCorruptionException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentAlreadyExistsException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentNotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class CsvRepository extends Repository<StorablePayment, String> {

    private final File file;
    private final CsvMapper csvMapper;
    private final Logger logger = LoggerFactory.getLogger(CsvRepository.class);

    public CsvRepository(final Supplier<String> idSupplier, final CsvMapper mapper, final File outputFile) throws IOException {
        super(idSupplier);
        file = outputFile;
        csvMapper = mapper;
    }

    @Override
    public StorablePayment save(final StorablePayment storablePayment) throws PaymentAlreadyExistsException {
        String uniqueId = storablePayment.getId();
        if (uniqueId == null) {
            uniqueId = this.supplyUniqueId();
            logger.debug("Generated unique id: " + uniqueId);
        }
        try {
            Optional<String> recordInFile;
            recordInFile = findRecordInFile(storablePayment, file);
            if (recordInFile.isPresent()) {
                throw new PaymentAlreadyExistsException("Payment with this id already exists");
            } else {
                StorablePayment build = updateStorablePaymentRecordId(storablePayment, uniqueId);
                FileWriter fileWriter = new FileWriter(file, true);
                CSVWriter csvWriter = new CSVWriter(fileWriter);
                csvWriter.writeNext(csvMapper.toCsv(build));
                csvWriter.close();
                fileWriter.close();
                return build;
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public StorablePayment update(final StorablePayment storablePayment) throws PaymentNotFoundException {
        try {
            Map<String, StorablePayment> collect = getStringStorablePaymentMap(file);
            StorablePayment oldEntry = collect.get(storablePayment.getId());
            if (oldEntry == null) {
                throw new PaymentNotFoundException("Payment not found");
            }
            collect.put(storablePayment.getId(), storablePayment);
            writeAllToCsv(collect, file, csvMapper);
            return storablePayment;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(final String id) throws PaymentNotFoundException {
        try {
            Map<String, StorablePayment> collect = getStringStorablePaymentMap(file);
            StorablePayment oldEntry = collect.get(id);
            if (oldEntry == null) {
                throw new PaymentNotFoundException("Payment not found");
            }
            collect.remove(id);
            writeAllToCsv(collect, file, csvMapper);
        } catch (IOException | CsvException e) {
            logger.error("File reading/writing exception. " + e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<StorablePayment> getById(final String id) {
        try {
            return Optional.ofNullable(getStringStorablePaymentMap(file).get(id));
        } catch (IOException | CsvException | PaymentNotFoundException e) {
            logger.error("File reading/writing exception. " + e.getLocalizedMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<StorablePayment> getAll() {
        try {
            Map<String, StorablePayment> stringStorablePaymentMap = getStringStorablePaymentMap(file);
            return stringStorablePaymentMap.values().stream().collect(Collectors.toUnmodifiableList());
        } catch (IOException | CsvException | PaymentNotFoundException e) {
            logger.error("File reading/writing exception. " + e.getLocalizedMessage());
        }
        return null;
    }


    private StorablePayment updateStorablePaymentRecordId(StorablePayment storablePayment, String uniqueId) {
        return StorablePayment.builder().accountNumber(storablePayment.getAccountNumber())
                .amount(storablePayment.getAmount()).currency(storablePayment.getCurrency())
                .userId(storablePayment.getUserId()).id(uniqueId).build();
    }

    private Optional<String> findRecordInFile(final StorablePayment storablePayment, final File file) throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new FileReader(file));
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if (storablePayment.getId().equals(nextLine[0])) {
                return Arrays.stream(nextLine).reduce((str1, str2) -> str1 + str2);
            }
        }
        return Optional.empty();
    }

    private Map<String, StorablePayment> getStringStorablePaymentMap(final File file) throws IOException, CsvException, PaymentNotFoundException {
        CSVReader reader = new CSVReader(new FileReader(file));
        List<String[]> allLines = reader.readAll();
        Map<String, StorablePayment> collect = allLines.stream().collect(Collectors.toMap(line -> line[0], csvMapper::fromCsv));
        reader.close();
        return collect;
    }

    private void writeAllToCsv(final Map<String, StorablePayment> collect, final File file, final CsvMapper csvMapper) throws IOException {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(file));
        csvWriter.writeAll(collect.values().stream().map(csvMapper::toCsv).collect(toList()));
        csvWriter.close();
    }
}
