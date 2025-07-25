package org.fantic.ethcrawler.dto.transactions;

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
        if (newTransactions != null)
            this.transactions.addAll(newTransactions);
    }
}
