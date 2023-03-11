package com.example.bankingapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
public class Controller {

  private BankingService bankingService;

  public Controller(BankingService bankingService) {
    this.bankingService = bankingService;
  }

  @Operation(summary = "Get an Account by its id")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Found the Account",
          content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Account.class))}),
      @ApiResponse(responseCode = "400", description = "Invalid id supplied", content = @Content),
      @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
  })
  @GetMapping("/account/{id}")
  public Account getAccount(@PathVariable(value = "id") UUID id) {
    return bankingService.getAccount(id);
  }

  @Operation(summary = "Create an Account with initial balance")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Account Created", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Account.class))}),
      @ApiResponse(responseCode = "400", description = "Account from does not have sufficient balance.", content = @Content)})
  @PostMapping("/account")
  public Account createAccount(@RequestParam(value = "customerId", required = true) UUID customerId,
                               @RequestParam(value = "balance", required = true) String initialBalance) {
    return bankingService.createAccount(customerId, initialBalance);
  }

  @Operation(summary = "Transfer an amount from an Account to another.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Transfer Completed"),
      @ApiResponse(responseCode = "400", description = "Account from does not have sufficient balance.", content = @Content)})
  @PostMapping("/transfer")
  public void transfer(@RequestParam(value = "fromId", required = true) UUID fromId,
                       @RequestParam(value = "toId", required = true) UUID toId,
                       @RequestParam(value = "amount", required = true) String amount) {
    bankingService.transfer(fromId, toId, amount);
  }

}
