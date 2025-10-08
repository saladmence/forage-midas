package com.jpmc.midascore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.foundation.Transaction;

@Service
public class IncentiveGetter {

    @Autowired
    private RestTemplate restTemplate = new RestTemplate();

    public IncentiveGetter() {
        System.out.println("incentive is being retrieved");
    }

    /**
     * Posts the transaction to the rest API and gets back an incentive
     * @param transaction
     * @return an Incentive object with the amount
     */
    public Incentive getIncentive(Transaction transaction) {
        Incentive incentive = restTemplate.postForObject("http://localhost:8080/incentive", transaction, Incentive.class);

        return incentive;
    }
}
