package pl.prokodzik.altmetrik.payments.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Currency;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class StorablePayment {
    private String id;
    private Long amount;
    private Currency currency;
    private String userId;
    private String accountNumber;
}
