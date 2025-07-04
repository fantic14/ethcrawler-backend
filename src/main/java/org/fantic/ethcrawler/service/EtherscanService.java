package org.fantic.ethcrawler.service;

import org.fantic.ethcrawler.dto.EthTransactionDto;
import org.fantic.ethcrawler.dto.TransactionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
                .scheme("https")
                .host("api.etherscan.io")
                .path("/api")
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
            String error = (response != null) ? String.valueOf(response.get("result")) : "No response from Etherscan";
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
                    .divide(new BigDecimal("1000000000000000000"));
            dto.setValue(ethValue.setScale(12, RoundingMode.DOWN).toPlainString() + " ETH");
            dto.setHash((String) tx.get("hash"));
            dto.setBlockNumber((String) tx.get("blockNumber"));
            dto.setTimeStamp((String) tx.get("timeStamp"));
            return dto;
        }).toList();

        return new TransactionResult(true, null, transactions);
    }
}
