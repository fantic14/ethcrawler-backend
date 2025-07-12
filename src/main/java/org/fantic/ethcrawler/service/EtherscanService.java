package org.fantic.ethcrawler.service;

import org.fantic.ethcrawler.dto.transactions.EthTransactionDto;
import org.fantic.ethcrawler.dto.transactions.TransactionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class EtherscanService {

    @Value("${etherscan.api.url}")
    private String etherscanApiUrl;

    @Value("${etherscan.api.key}")
    private String etherscanApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public TransactionResult getNormalTransactions(String walletAddress, String startBlock) {
        String url = UriComponentsBuilder.newInstance()
                .uri(URI.create(etherscanApiUrl))
                .queryParam("module", "account")
                .queryParam("action", "txlist")
                .queryParam("address", walletAddress)
                .queryParam("startblock", startBlock)
                .queryParam("endblock", "99999999")
                .queryParam("sort", "asc")
                .queryParam("apikey", etherscanApiKey)
                .toUriString();

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !"1".equals(String.valueOf(response.get("status")))) {
            String error = (response != null && !String.valueOf(response.get("result")).isEmpty()) ? String.valueOf(response.get("result")) : "No response from Etherscan";
            return new TransactionResult(false, error, null);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> resultDtos = (List<Map<String, Object>>) response.get("result");

        List<EthTransactionDto> transactions = resultDtos.stream().map(tx -> {
            EthTransactionDto dto = new EthTransactionDto();
            dto.setFrom((String) tx.get("from"));
            dto.setTo((String) tx.get("to"));
            String valueInWei = tx.get("value").toString();
            BigDecimal ethValue = new BigDecimal(valueInWei)
                    .divide(BigDecimal.TEN.pow(18), 15, RoundingMode.HALF_DOWN);
            dto.setValue(ethValue.toPlainString() + " ETH");
            dto.setHash((String) tx.get("hash"));
            dto.setBlockNumber((String) tx.get("blockNumber"));
            dto.setTimeStamp((String) tx.get("timeStamp"));
            long transactionFeeInWei = (Long.parseLong((String) tx.get("gas")) * Long.parseLong((String) tx.get("gasPrice")));
            BigDecimal transactionFee = new BigDecimal(transactionFeeInWei)
                   .divide(BigDecimal.TEN.pow(18), 15, RoundingMode.HALF_DOWN);
            dto.setTransactionFee(transactionFee.toPlainString() + " ETH");
            return dto;
        }).toList();

        return new TransactionResult(true, null, transactions);
    }

    public Map getBlockNoByTime(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        long unixTimestamp = localDate.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC);

        String url = UriComponentsBuilder.newInstance()
                .uri(URI.create(etherscanApiUrl))
                .queryParam("module", "block")
                .queryParam("action", "getblocknobytime")
                .queryParam("timestamp", unixTimestamp)
                .queryParam("closest", "before")
                .queryParam("apikey", etherscanApiKey)
                .toUriString();

        return restTemplate.getForObject(url, Map.class);
    }
}
