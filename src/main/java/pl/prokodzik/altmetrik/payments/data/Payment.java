package pl.prokodzik.altmetrik.payments.data;

import lombok.Getter;
import lombok.Setter;

import java.util.Currency;

@Getter
@Setter
public class Payment {
    private String id;
    private long amount;
    private Currency currency;
    private String userId;
    private String accountNumber;
}
