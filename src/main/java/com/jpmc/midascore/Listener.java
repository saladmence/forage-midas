package com.jpmc.midascore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.TransactionRecord;
import com.jpmc.midascore.TransactionRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;


@Component
public class Listener {
    @Autowired 
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncentiveGetter incentiveGetter;

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core")
    public void listen(String message) {
        try {
            JsonNode node = new ObjectMapper().readTree(message);
            Long senderId = node.get("senderId").asLong();
            Long recipientId = node.get("recipientId").asLong();
            float amount = node.get("amount").floatValue();

            // validate sender id 
            if (senderId < 0) {
                throw new IllegalArgumentException("senderId is not valid");
            }

            // validate recipient id
            if (recipientId < 0) {
                throw new IllegalArgumentException("recipientId is not valid");
            }

            // validate amount
            if (amount < 0) {
                throw new IllegalArgumentException("transaction amount is not valid");
            }

            Optional<UserRecord> senderDetails = userRepository.findById(senderId);
            Optional<UserRecord> recipientDetails = userRepository.findById(recipientId);

            if (senderDetails.isPresent() && recipientDetails.isPresent()) {
                UserRecord sender = senderDetails.get();
                UserRecord recipient = recipientDetails.get();

                if (sender.getBalance() < amount) {
                    throw new IllegalArgumentException("invalid transaction from sender " + sender.getName() + " with "+ sender.getBalance() + ", trying to send " + amount);
                } else {
                    // executes transaction
                    sender.setBalance(sender.getBalance() - amount);
                    recipient.setBalance(recipient.getBalance() + amount);

                    TransactionRecord record = new TransactionRecord();
                    record.setSender(sender);
                    record.setRecipient(recipient);
                    record.setAmount(amount);

                    transactionRepository.save(record);

                    Transaction transaction = new Transaction(senderId, recipientId, amount);
                    Incentive incentive = incentiveGetter.getIncentive(transaction);

                    recipient.setBalance(recipient.getBalance() + incentive.getAmount());
                    userRepository.save(sender);
                    userRepository.save(recipient);

                    printAllUsers();

                }

            } else {
                System.err.println("sender/recipient doesn't exist");
                throw new IllegalArgumentException("Sender/Recipient not found");
            }
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void printAllUsers() {
        List<UserRecord> users = (List<UserRecord>) userRepository.findAll();
        System.out.println("=== Users Table ===");
        for (UserRecord u : users) {
            System.out.printf("ID: %d, Username: %s, Balance: %.2f%n",
                                u.getId(), u.getName(), u.getBalance());
        }
        System.out.println("==================");
    }
}
