package com.bytebridges.anytop.domain.eloadengine.external;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.bytebridges.anytop.config.EloadConfig;
import com.bytebridges.anytop.domain.eloadengine.exception.UssdGatewayException;
import com.bytebridges.anytop.domain.transaction.dto.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * External USSD Gateway client.
 *
 * Handles HTTP communication with the USSD Gateway service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UssdGatewayClient {

	private final WebClient webClient;
	private final ObjectMapper mapper;
	private final EloadConfig config;

	public Message sendUssd(String path, String port, String ussd) {

		try {
			String response = webClient.get()
					.uri(uriBuilder -> uriBuilder.path(path)
							.queryParam("username", config.getUsername())
							.queryParam("password", config.getPassword())
							.queryParam("port", port)
							.queryParam("ussd", ussd).build())
					.retrieve()
					.onStatus(status -> status.isError(),
							clientResponse -> clientResponse.bodyToMono(String.class)
									.map(body -> new RuntimeException("USSD error response: " + body)))
					.bodyToMono(String.class)
					//.block(Duration.ofSeconds(30));
			        .block();
			return mapper.readValue(response, Message.class);
		} catch (Exception e) {
			log.error("USSD gateway error port={} ussd={} errorType={}", port, ussd, e.getClass().getSimpleName(), e);
			throw new UssdGatewayException("USSD Gateway error", e);
		}
	}
}