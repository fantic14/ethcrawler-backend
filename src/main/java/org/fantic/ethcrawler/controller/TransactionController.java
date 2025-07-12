package org.fantic.ethcrawler.controller;

import jakarta.validation.constraints.NotBlank;
import org.fantic.ethcrawler.dto.transactions.TransactionResult;
import org.fantic.ethcrawler.service.EtherscanService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class TransactionController {

    private final EtherscanService etherscanService;

    public TransactionController(EtherscanService etherscanService) {
        this.etherscanService = etherscanService;
    }

    @GetMapping("/transactions")
    public TransactionResult getTransactions(
            @RequestParam("walletAddress") @NotBlank String walletAddress,
            @RequestParam("startBlock") @NotBlank String startBlock
    ) {

        // TODO make this faster ? Parallel requests (watch out for the limit of 5 calls per second) ?

        TransactionResult transactionResult = new TransactionResult(true, null, new ArrayList<>());

        try {
            int newStart = Integer.parseInt(startBlock.trim());
            TransactionResult temp;
            boolean first = true;
            while (true) {
                temp = etherscanService.getNormalTransactions(walletAddress.trim(), String.valueOf(newStart));
                transactionResult.addToTransactions(temp.getTransactions());
                if (first && temp.getErrorMessage() != null) {
                    return new TransactionResult(false, temp.getErrorMessage(), null);
                }
                first = false;
                if (temp.getTransactions() != null && temp.getTransactions().size() < 10000) break;
                if (temp.getTransactions() != null)
                    newStart = Integer.parseInt(temp.getTransactions().getLast().getBlockNumber()) + 1;
                else break;
            }
        } catch (NumberFormatException e) {
            System.out.println("entered numberformat exception");
            return new TransactionResult(false, "Start block not an integer", null);
        }
        return transactionResult;
    }
}
