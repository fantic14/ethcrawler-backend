package org.fantic.ethcrawler.service;

import org.fantic.ethcrawler.dto.balance.AccountBalanceDto;
import org.fantic.ethcrawler.dto.balance.AccountBalanceResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class InfuraService {

    private final EtherscanService etherscanService;
    @Value("${mainnet.infura.api.url.with.key}")
    private String mainnetInfuraApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public InfuraService(EtherscanService etherscanService) {
        this.etherscanService = etherscanService;
    }

    public AccountBalanceResult getNormalBalance(String walletAddress, String date, String token) {

        Map response = etherscanService.getBlockNoByTime(date);

        if (response == null || !"1".equals(String.valueOf(response.get("status")))) {
            String error = (response != null && !String.valueOf(response.get("result")).isEmpty()) ? String.valueOf(response.get("result")) : "No response from Etherscan";
            return new AccountBalanceResult(false, error, null);
        }

        int blockNo = Integer.parseInt((String) response.get("result"));
        String blockNoHex = Integer.toHexString(blockNo);

        Map<String, Object> requestBody = getBodyForToken(walletAddress, blockNoHex, token);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        response = restTemplate.postForEntity(mainnetInfuraApiUrl, entity, Map.class).getBody();

        if (response == null || response.containsKey("error")) {
            String error = (response != null && !String.valueOf(response.get("error")).isEmpty()) ? String.valueOf(response.get("error")) : "No response from Infura";
            return new AccountBalanceResult(false, error, null);
        }

        AccountBalanceDto accBalance;
        Integer balance = Integer.parseInt(((String) response.get("result")).substring(2), 16);
        String balanceInCurrency = "";

        switch (token) {
            case "eth": case "dai": case "link": {
                balanceInCurrency = new BigDecimal(String.valueOf(balance))
                        .divide(BigDecimal.TEN.pow(18), 15, RoundingMode.HALF_DOWN)
                        .toPlainString();
                break;
            }
            case "usdt": case "usdc": {
                balanceInCurrency = new BigDecimal(String.valueOf(balance))
                        .divide(BigDecimal.TEN.pow(6), 15, RoundingMode.HALF_DOWN)
                        .toPlainString();
                break;
            }
        }

        accBalance = new AccountBalanceDto(walletAddress, date, balanceInCurrency + " " + token.toUpperCase());

        return new AccountBalanceResult(true, null, accBalance);
    }

    private Map<String, Object> getBodyForToken(String walletAddress, String blockNoHex, String token) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("jsonrpc", "2.0");

        if (token.equals("eth")) {
            requestBody.put("method", "eth_getBalance");
            requestBody.put("params", Arrays.asList(walletAddress, "0x" + blockNoHex));
            requestBody.put("id", 1);
        } else {
            Map<String, String> params = new HashMap<>();
            requestBody.put("method", "eth_call");
            params.put("data", "0x70a08231000000000000000000000000" + walletAddress.substring(2));

            switch (token) {
                case "usdt": {
                    params.put("to", "0xdAC17F958D2ee523a2206206994597C13D831ec7");
                    requestBody.put("params", Arrays.asList(params, "0x" + blockNoHex));
                    requestBody.put("id", 2);
                    break;
                }
                case "usdc": {
                    params.put("to", "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48");
                    requestBody.put("params", Arrays.asList(params, "0x" + blockNoHex));
                    requestBody.put("id", 3);
                    break;
                }
                case "dai": {
                    params.put("to", "0x6B175474E89094C44Da98b954EedeAC495271d0F");
                    requestBody.put("params", Arrays.asList(params, "0x" + blockNoHex));
                    requestBody.put("id", 4);
                    break;
                }
                case "link": {
                    params.put("to", "0x514910771AF9Ca656af840dff83E8264EcF986CA");
                    requestBody.put("params", Arrays.asList(params, "0x" + blockNoHex));
                    requestBody.put("id", 5);
                    break;
                }
                default:
                    return null;
            }
        }
        return requestBody;
    }
}
