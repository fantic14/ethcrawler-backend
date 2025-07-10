package org.fantic.ethcrawler.dto.balance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountBalanceResult {

    private boolean statusMessage;
    private String errorMessage;
    private AccountBalanceDto accountBalance;
}
