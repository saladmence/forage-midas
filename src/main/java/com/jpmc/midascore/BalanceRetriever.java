package com.jpmc.midascore;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;

@RestController
public class BalanceRetriever {

    @Autowired
    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepository;


    public BalanceRetriever(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @GetMapping("/balance")
    public Balance getUserBalance(@RequestParam("userId") long userId) {
        float amount = 0;

        if (userId > 0) {
            Optional<UserRecord> maybeUser = userRepository.findById(Long.valueOf(userId));
            if (maybeUser.isPresent()) {
                amount = maybeUser.get().getBalance();
            }
        }

        return new Balance(amount);
    }
}
