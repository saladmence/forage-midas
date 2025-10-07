package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import jakarta.persistence.*;

@Entity
public class TransactionRecord { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private UserRecord sender;

    @ManyToOne
    private UserRecord recipient;

    private float amount;

    public void setSender(UserRecord sender) {
        this.sender = sender;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setRecipient(UserRecord recipient) {
        this.recipient = recipient;
    }
}
