package com.example.bankingapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class BankingService {

  public static final String BAD_TRANSFER_REQUEST_INSUFFICIENT_FUNDS = "From Account %s does not have enough balance. Balance is %s";
  public static final String BAD_TRANSFER_REQUEST_NEGATIVE_TRANSFER_AMOUNT = "Amount, %s, is not greater than 0.";
  public static final String TRANSFER_MESSAGE = "Transferred %s from %s to %s";

  public BankingService() {
    this.accounts = new HashMap<>();
  }

  private Map<UUID, Account> accounts;

  private BigDecimal parseAmount(String amount) {
    try {
      return new BigDecimal(amount);
    } catch (NumberFormatException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Amount , " + amount + " is not a valid amount."
      );
    }
  }

  public void transfer(UUID fromId, UUID toId, String amountString) {
    BigDecimal amount = parseAmount(amountString);
    if (amount.compareTo(new BigDecimal(0)) <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(BankingService.BAD_TRANSFER_REQUEST_NEGATIVE_TRANSFER_AMOUNT, amount));
    }

    if (fromId.equals(toId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't transfer money to the same account.");
    }
    if (!accounts.containsKey(fromId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From Account " + fromId + " not found.");
    }
    if (!accounts.containsKey(toId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "To Account " + toId + " not found.");
    }

    Account from = accounts.get(fromId);

    BigDecimal fromBalance = from.getBalance();

    if (amount.compareTo(fromBalance) > 0) {
      String reason = String.format(BAD_TRANSFER_REQUEST_INSUFFICIENT_FUNDS, fromId, fromBalance);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    BigDecimal newFromBalance = fromBalance.subtract(amount);
    from.setBalance(newFromBalance);
    String transfer = String.format(TRANSFER_MESSAGE, amount, fromId, toId);
    from.getTransfers().add(transfer);

    Account to = accounts.get(toId);
    BigDecimal newToBalance = to.getBalance().add(amount);
    to.setBalance(newToBalance);
    to.getTransfers().add(transfer);

    log.info("Account transfer complete: {}", transfer);
  }

  public Account createAccount(UUID customerId, String initialBalanceString) {
    BigDecimal initialBalance = parseAmount(initialBalanceString);
    UUID id = UUID.randomUUID();
    if (initialBalance.intValue() < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Balance, " + initialBalance + ", is less than 0.");
    }
    Account account = new Account(id, initialBalance, customerId);
    accounts.put(id, account);
    log.info("Account created: {}", account);
    return account;
  }

  public Account getAccount(UUID id) {
    if (!accounts.containsKey(id)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Account " + id + " not found."
      );
    }
    return accounts.get(id);
  }
}
