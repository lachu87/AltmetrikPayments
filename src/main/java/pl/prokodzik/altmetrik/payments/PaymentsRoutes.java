package pl.prokodzik.altmetrik.payments;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.prokodzik.altmetrik.payments.exceptions.InputDataException;
import pl.prokodzik.altmetrik.payments.exceptions.PaymentNotFoundException;
import pl.prokodzik.altmetrik.payments.service.PaymentInput;
import pl.prokodzik.altmetrik.payments.service.PaymentsService;
import pl.prokodzik.altmetrik.payments.service.SimplePaymentsService;
import pl.prokodzik.altmetrik.payments.storage.CsvMapper;
import pl.prokodzik.altmetrik.payments.storage.CsvRepository;
import pl.prokodzik.altmetrik.payments.storage.InMemRepository;
import pl.prokodzik.altmetrik.payments.storage.Repository;
import pl.prokodzik.altmetrik.payments.storage.StorablePayment;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.patch;
import static spark.Spark.port;
import static spark.Spark.post;

public class PaymentsRoutes {


    public static final String DEFAULT_PATH = "/payments";
    private static final Supplier<String> idSupplier = () -> UUID.randomUUID().toString();

    public static void main(String[] args) throws IOException {

        final Gson gson = new GsonBuilder().serializeNulls().create();

        final Repository<StorablePayment, String> repository;

        repository = getRepository(args[0], idSupplier);
        final PaymentsService<PaymentInput, StorablePayment, String> paymentsService = new SimplePaymentsService(repository);

        port(8080);

        // Routes

        get(DEFAULT_PATH + "/:id", (req, resp) -> {
            String paymentId = req.params().get(":id");
            StorablePayment byId = paymentsService.getById(paymentId);
            return gson.toJson(byId);
        });

        get(DEFAULT_PATH, (req, resp) -> {
            List<StorablePayment> all = paymentsService.getAll();
            return gson.toJson(all);
        });

        post(DEFAULT_PATH, (req, resp) -> {
            String paymentInputJson = req.body();
            PaymentInput paymentInput = gson.fromJson(paymentInputJson, PaymentInput.class);
            StorablePayment save = paymentsService.save(paymentInput);
            return gson.toJson(save);
        });

        delete(DEFAULT_PATH + "/:id", (req, resp) -> {
            String paymentId = req.params().get(":id");
            paymentsService.delete(paymentId);
            return "";
        });

        patch(DEFAULT_PATH + "/:id", (req, resp) -> {
            String paymentInputJson = req.body();
            String paymentId = req.params().get(":id");
            PaymentInput paymentInput = gson.fromJson(paymentInputJson, PaymentInput.class);
            StorablePayment update = paymentsService.update(paymentInput, paymentId);
            return gson.toJson(update);
        });


        // Exception handling

        exception(PaymentNotFoundException.class, (exception, request, response) -> {
            response.status(404);
            response.body(exception.getLocalizedMessage());
        });

        exception(InputDataException.class, (exception, request, response) -> {
            response.status(422);
            response.body(exception.getLocalizedMessage());
        });
    }

    private static Repository<StorablePayment, String> getRepository(String arg, Supplier<String> idSupplier) throws IOException {
        Repository<StorablePayment, String> repository;
        switch (arg) {
            case "CSV": {
                final File repo = new File("repo.csv");
                if(!repo.exists()) {
                    repo.createNewFile();
                }
                repository = new CsvRepository(idSupplier, new CsvMapper(), repo);
                return repository;
            }
            case "MEM":
            default: return new InMemRepository(idSupplier);
        }

    }

}
