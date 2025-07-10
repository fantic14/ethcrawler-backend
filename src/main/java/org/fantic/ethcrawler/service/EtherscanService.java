package org.fantic.ethcrawler.service;

import org.fantic.ethcrawler.dto.balance.AccountBalanceDto;
import org.fantic.ethcrawler.dto.balance.AccountBalanceResult;
import org.fantic.ethcrawler.dto.transactions.EthTransactionDto;
import org.fantic.ethcrawler.dto.transactions.TransactionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EtherscanService {

    @Value("${etherscan.api.url}")
    private String etherscanApiUrl;

    @Value("${etherscan.api.key}")
    private String etherscanApiKey;

    @Value("${mainnet.infura.api.url.with.key}")
    private String mainnetInfuraApiUrl;

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
            String valueInWei =tx.get("value").toString();
            BigDecimal ethValue = new BigDecimal(valueInWei)
                    .divide(new BigDecimal("1000000000000000000"), 15, RoundingMode.HALF_DOWN);
            dto.setValue(ethValue.toPlainString() + " ETH");
            dto.setHash((String) tx.get("hash"));
            dto.setBlockNumber((String) tx.get("blockNumber"));
            dto.setTimeStamp((String) tx.get("timeStamp"));
            return dto;
        }).toList();

        return new TransactionResult(true, null, transactions);
    }

    public AccountBalanceResult getNormalBalance(String walletAddress, String date) {

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

        Map response = restTemplate.getForObject(url, Map.class);

        if (response == null || !"1".equals(String.valueOf(response.get("status")))) {
            String error = (response != null && !String.valueOf(response.get("result")).isEmpty()) ? String.valueOf(response.get("result")) : "No response from Etherscan";
            return new AccountBalanceResult(false, error, null);
        }

        int blockNo = Integer.parseInt((String) response.get("result"));
        String blockNoHex = Integer.toHexString(blockNo);

        // Here using infura because just here I saw that getting
        // balance on certain block is a pro feature in etherscan...

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("jsonrpc", "2.0");
        requestBody.put("method", "eth_getBalance");
        requestBody.put("params", Arrays.asList(walletAddress, "0x" + blockNoHex));
        requestBody.put("id", 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        response = restTemplate.postForEntity(mainnetInfuraApiUrl, entity, Map.class).getBody();

        if (response == null || response.containsKey("error")) {
            String error = (response != null && !String.valueOf(response.get("error")).isEmpty()) ? String.valueOf(response.get("error")) : "No response from Infura";
            return new AccountBalanceResult(false, error, null);
        }

        Integer balance = Integer.parseInt(((String) response.get("result")).substring(2), 16);
        System.out.println(balance);
        String balanceInEth = new BigDecimal(String.valueOf(balance))
                .divide(new BigDecimal("1000000000000000000"), 15, RoundingMode.HALF_DOWN)
                .toPlainString();
        AccountBalanceDto accBalance = new AccountBalanceDto(walletAddress, date, balanceInEth + " ETH");
        return new AccountBalanceResult(true, null, accBalance);
    }
}
