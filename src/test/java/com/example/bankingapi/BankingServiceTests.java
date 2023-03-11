package com.example.bankingapi;


import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BankingServiceTests {

  private BankingService service = new BankingService();

  @Test
  void createAccountWithInvalidRequest() {
    UUID cust1Id = UUID.randomUUID();
    ResponseStatusException badRequest = assertThrows(ResponseStatusException.class, () -> service.createAccount(cust1Id, "-1"));
    assertThat(badRequest.getStatusCode(), equalTo(HttpStatusCode.valueOf(400)));
    assertThat(badRequest.getReason(), equalTo("Balance, -1, is less than 0."));

    badRequest = assertThrows(ResponseStatusException.class, () -> service.createAccount(cust1Id, "100,0"));
    assertThat(badRequest.getStatusCode(), equalTo(HttpStatusCode.valueOf(400)));
    assertThat(badRequest.getReason(), equalTo("Amount , 100,0 is not a valid amount."));
  }

  @Test
  void accounts() {
    UUID cust1Id = UUID.randomUUID();
    UUID cust2Id = UUID.randomUUID();
    UUID account1Id = service.createAccount(cust1Id, "0").getId();
    UUID account2Id = service.createAccount(cust2Id, "10").getId();
    UUID account3Id = service.createAccount(cust2Id, "100").getId();

    Account account1 = service.getAccount(account1Id);
    assertThat(account1, hasProperty("id", equalTo(account1Id)));
    assertThat(account1, hasProperty("balance", equalTo(new BigDecimal("0"))));
    assertThat(account1, hasProperty("customerId", equalTo(cust1Id)));
    List<String> account1Transfers = account1.getTransfers();
    assertThat(account1Transfers, contains("Balance is 0"));

    Account account2 = service.getAccount(account2Id);
    assertThat(account2, hasProperty("id", equalTo(account2Id)));
    assertThat(account2, hasProperty("balance", equalTo(new BigDecimal("10"))));
    assertThat(account2, hasProperty("customerId", equalTo(cust2Id)));
    List<String> account2Transfers = account2.getTransfers();
    assertThat(account2Transfers, contains("Balance is 10"));

    Account account3 = service.getAccount(account3Id);
    assertThat(account3, hasProperty("id", equalTo(account3Id)));
    assertThat(account3, hasProperty("balance", equalTo(new BigDecimal("100"))));
    assertThat(account3, hasProperty("customerId", equalTo(cust2Id)));
    List<String> account3Transfers = account3.getTransfers();
    assertThat(account3Transfers, contains("Balance is 100"));

    //transfer 10 to from account 1 to 2 fails
    ResponseStatusException badRequest = assertThrows(ResponseStatusException.class, () -> service.transfer(account1Id, account2Id, "10"));
    assertThat(badRequest.getStatusCode(), equalTo(HttpStatusCode.valueOf(400)));
    assertThat(badRequest.getReason(), equalTo(String.format(BankingService.BAD_TRANSFER_REQUEST_INSUFFICIENT_FUNDS, account1Id, 0)));

    //transfer negative amount fails
    badRequest = assertThrows(ResponseStatusException.class, () -> service.transfer(account1Id, account2Id, "-10"));
    assertThat(badRequest.getStatusCode(), equalTo(HttpStatusCode.valueOf(400)));
    assertThat(badRequest.getReason(), equalTo(String.format(BankingService.BAD_TRANSFER_REQUEST_NEGATIVE_TRANSFER_AMOUNT, -10)));

    //transfer 10 from missing to account 2 fails
    UUID missingAcct = UUID.randomUUID();
    badRequest = assertThrows(ResponseStatusException.class, () -> service.transfer(missingAcct, account2Id, "10"));
    assertThat(badRequest.getStatusCode(), equalTo(HttpStatusCode.valueOf(400)));
    assertThat(badRequest.getReason(), equalTo("From Account " + missingAcct + " not found."));

    //transfer 10 from account 2 to missing fails
    badRequest = assertThrows(ResponseStatusException.class, () -> service.transfer(account2Id, missingAcct, "10"));
    assertThat(badRequest.getStatusCode(), equalTo(HttpStatusCode.valueOf(400)));
    assertThat(badRequest.getReason(), equalTo("To Account " + missingAcct + " not found."));

    //transfer 10 from account 2 to account 2 fails
    badRequest = assertThrows(ResponseStatusException.class, () -> service.transfer(account2Id, account2Id, "10"));
    assertThat(badRequest.getStatusCode(), equalTo(HttpStatusCode.valueOf(400)));

    //transfer 10 from acct 2 to account 1
    service.transfer(account2Id, account1Id, "10");
    account1 = service.getAccount(account1Id);
    assertThat(account1, hasProperty("id", equalTo(account1Id)));
    assertThat(account1, hasProperty("balance", equalTo(new BigDecimal("10"))));
    assertThat(account1, hasProperty("customerId", equalTo(cust1Id)));
    account1Transfers = account1.getTransfers();
    assertThat(account1Transfers, contains("Balance is 0", String.format(BankingService.TRANSFER_MESSAGE, 10, account2Id, account1Id)));

    account2 = service.getAccount(account2Id);
    assertThat(account2, hasProperty("id", equalTo(account2Id)));
    assertThat(account2, hasProperty("balance", equalTo(new BigDecimal("0"))));
    assertThat(account2, hasProperty("customerId", equalTo(cust2Id)));
    account2Transfers = account2.getTransfers();
    assertThat(account2Transfers, contains("Balance is 10", String.format(BankingService.TRANSFER_MESSAGE, 10, account2Id, account1Id)));
  }
}