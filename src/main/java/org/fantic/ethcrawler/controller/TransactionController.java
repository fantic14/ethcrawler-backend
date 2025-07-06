package org.fantic.ethcrawler.controller;

import jakarta.validation.constraints.NotBlank;
import org.fantic.ethcrawler.dto.TransactionResult;
import org.fantic.ethcrawler.service.EtherscanService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

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
        TransactionResult transactionResult = new TransactionResult(true, null, new ArrayList<>());
        try {
            int newStart = Integer.parseInt(startBlock);
            TransactionResult temp;
            while (true) {
               temp = etherscanService.getNormalTransactions(walletAddress, String.valueOf(newStart));
               Thread.sleep(200);
               if (temp.getErrorMessage() != null) break;
               transactionResult.addToTransactions(temp.getTransactions());
               if (temp.getTransactions().size() < 10000) break;
               newStart += 10000;
            }
        } catch (NumberFormatException e) {
            return new TransactionResult(false, "Start block not an integer", null);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return transactionResult;
    }
}
