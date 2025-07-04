package org.fantic.ethcrawler.controller;

import jakarta.validation.constraints.NotBlank;
import org.fantic.ethcrawler.dto.EthTransactionDto;
import org.fantic.ethcrawler.dto.TransactionResult;
import org.fantic.ethcrawler.service.EtherscanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TransactionController {

    private final EtherscanService etherscanService;

    public TransactionController(EtherscanService etherscanService) {
        this.etherscanService = etherscanService;
    }

    @GetMapping("/")
    public String showForm() {
        return "transaction-form";
    }

    @PostMapping("/transactions")
    public String getTransactions (
            @RequestParam("walletAddress") @NotBlank String walletAddress,
            @RequestParam("startBlock") @NotBlank String startBlock,
            Model model
    ) {
        TransactionResult result = etherscanService.getNormalTransactions(walletAddress, startBlock);
        if(!result.isSuccess()){
            model.addAttribute("errorMessage", result.getErrorMessage());
            return "transaction-form";
        }
        model.addAttribute("transactions", result.getTransactions());
        model.addAttribute("walletAddress", walletAddress);
        return "transaction-result";
    }
}
