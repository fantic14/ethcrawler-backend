package org.fantic.ethcrawler.controller;

import jakarta.validation.constraints.NotBlank;
import org.fantic.ethcrawler.dto.balance.AccountBalanceResult;
import org.fantic.ethcrawler.service.EtherscanService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class BalanceController {

    private EtherscanService etherscanService;

    public BalanceController(EtherscanService etherscanService) {
        this.etherscanService = etherscanService;
    }

    @GetMapping("/balance")
    public AccountBalanceResult getBalance(
            @RequestParam("walletAddress") @NotBlank String walletAddress,
            @RequestParam("date") @NotBlank String date
    ) {
            AccountBalanceResult accountBalanceResult;

            accountBalanceResult = this.etherscanService.getNormalBalance(walletAddress, date);

            return accountBalanceResult;
    }
}
