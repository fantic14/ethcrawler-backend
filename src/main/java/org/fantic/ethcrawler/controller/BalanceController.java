package org.fantic.ethcrawler.controller;

import jakarta.validation.constraints.NotBlank;
import org.fantic.ethcrawler.dto.balance.AccountBalanceResult;
import org.fantic.ethcrawler.service.InfuraService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class BalanceController {

    private final InfuraService infuraService;

    public BalanceController(InfuraService infuraService) {
        this.infuraService = infuraService;
    }

    @GetMapping("/balance")
    public AccountBalanceResult getBalance(
            @RequestParam("walletAddress") @NotBlank String walletAddress,
            @RequestParam("date") @NotBlank String date,
            @RequestParam("token") @NotBlank String token
    ) {

        return this.infuraService.getNormalBalance(walletAddress.trim(), date.trim(), token.trim());
    }
}
