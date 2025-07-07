package org.fantic.ethcrawler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResult {
    private boolean statusMessage;
    private String errorMessage;
    private List<EthTransactionDto> transactions;

    public void addToTransactions(List<EthTransactionDto> newTransactions) {
        this.transactions.addAll(newTransactions);
    }
}
