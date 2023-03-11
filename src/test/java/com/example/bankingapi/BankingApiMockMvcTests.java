package com.example.bankingapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes=BankingApiApplication.class)
@AutoConfigureMockMvc
class BankingApiMockMvcTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper mapper;

	@Test
	void accounts() throws Exception {
		UUID customerId = UUID.randomUUID();
		String balance = "0";

		//missing create request params returns 400
		mockMvc.perform(post("/account").param("customerId", String.valueOf(customerId))).andExpect(status().isBadRequest());
		mockMvc.perform(post("/account").param("balance", "1")).andExpect(status().isBadRequest());

		//get missing account returns 404
		mockMvc.perform(get("/account/{id}", UUID.randomUUID())).andExpect(status().isNotFound());

		//create
		String responseBody = mockMvc.perform(post("/account")
						.param("customerId", customerId.toString())
						.param("balance", String.valueOf(balance)))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.balance", equalTo(0)))
				.andExpect(jsonPath("$.customerId", equalTo(customerId.toString())))
				.andReturn()
				.getResponse()
				.getContentAsString();

		Account account1 = mapper.readValue(responseBody, Account.class);

		//get
		mockMvc.perform(get("/account/{id}", account1.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id", equalTo(account1.getId().toString())))
				.andExpect(jsonPath("$.balance", equalTo(account1.getBalance().intValue())))
				.andExpect(jsonPath("$.customerId", equalTo(account1.getCustomerId().toString())));

	}

}
