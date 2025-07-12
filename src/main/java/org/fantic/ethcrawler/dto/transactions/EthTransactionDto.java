package org.fantic.ethcrawler.dto.transactions;

import lombok.Data;

@Data
public class EthTransactionDto {

    private String from;
    private String to;
    private String value;
    private String hash;
    private String blockNumber;
    private String timeStamp;
    private String transactionFee;
}
