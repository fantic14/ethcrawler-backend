package org.fantic.ethcrawler.dto.balance;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountBalanceDto {

    private String walletAddress;
    private String date;
    private String balance;
}
