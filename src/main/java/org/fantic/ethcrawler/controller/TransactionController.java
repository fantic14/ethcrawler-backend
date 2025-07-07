package org.fantic.ethcrawler.controller;

import jakarta.validation.constraints.NotBlank;
import org.fantic.ethcrawler.dto.TransactionResult;
import org.fantic.ethcrawler.service.EtherscanService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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

        try (ExecutorService executor = Executors.newFixedThreadPool(5)) {

            List<Future<TransactionResult>> futures = new ArrayList<>();
            List<TransactionResult> temps = new ArrayList<>();
            int newStart = Integer.parseInt(startBlock);
            boolean first = true;

            outerloop:
            while (true) {
                for (int i = 0; i < 5; i++) {
                    // TODO how to figure out what will be the last block in previous transaction list before it executes?
                    Callable<TransactionResult> task = () -> etherscanService.getNormalTransactions(walletAddress, String.valueOf(), )
                    futures.add(executor.submit(task));
                }

                for (Future<TransactionResult> future : futures) {
                    temps.add(future.get());
                }
                if (first) {
                    TransactionResult firstTransaction = temps.getFirst();
                    for (TransactionResult temp : temps) {
                        if (Integer.parseInt(temp.getTransactions().getFirst().getBlockNumber()) < Integer.parseInt(firstTransaction.getTransactions().getFirst().getBlockNumber()))
                            firstTransaction = temp;
                    }
                    if (!firstTransaction.isStatusMessage())
                        return new TransactionResult(false, firstTransaction.getErrorMessage(), null);
                    first = false;
                }
                for (TransactionResult temp : temps) {
                    transactionResult.addToTransactions(temp.getTransactions());
                    if (temp.getTransactions() != null && temp.getTransactions().size() < 10000) break outerloop;
                }

                TransactionResult lastTrasaction = temps.getFirst();
                for (TransactionResult temp : temps) {
                    if (Integer.parseInt(temp.getTransactions().getFirst().getBlockNumber()) > Integer.parseInt(lastTrasaction.getTransactions().getFirst().getBlockNumber()))
                        lastTrasaction = temp;
                }
                if (lastTrasaction.getTransactions() != null)
                    newStart = Integer.parseInt(lastTrasaction.getTransactions().getLast().getBlockNumber())+1;
                else break;
            }
        } catch (NumberFormatException e) {
            System.out.println("entered numberformat exception");
            return new TransactionResult(false, "Start block not an integer", null);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException();
        }
        return transactionResult;
    }
}
