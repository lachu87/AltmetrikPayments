package pl.prokodzik.altmetrik.payments.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentInput {
    private Long amount;
    private String currency;
    private String userId;
    private String accountNumber;
}
