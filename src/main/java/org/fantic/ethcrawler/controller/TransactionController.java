package org.fantic.ethcrawler.controller;

import jakarta.validation.constraints.NotBlank;
import org.fantic.ethcrawler.dto.TransactionResult;
import org.fantic.ethcrawler.service.EtherscanService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class TransactionController {

    private final EtherscanService etherscanService;

    public TransactionController(EtherscanService etherscanService) {
        this.etherscanService = etherscanService;
    }

    @GetMapping("/transactions")
    public TransactionResult getTransactions (
            @RequestParam("walletAddress") @NotBlank String walletAddress,
            @RequestParam("startBlock") @NotBlank String startBlock
    ) {
        return etherscanService.getNormalTransactions(walletAddress, startBlock);
    }
}
