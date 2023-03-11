package com.example.bankingapi;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Account {

  private UUID id;
  private BigDecimal balance;
  private UUID customerId;

  private List<String> transfers;

  public Account(UUID id, BigDecimal balance, UUID customerId) {
    this.id = id;
    this.balance = balance;
    this.customerId = customerId;
    this.transfers = new ArrayList<>();
    this.transfers.add("Balance is " + balance);
  }
}
