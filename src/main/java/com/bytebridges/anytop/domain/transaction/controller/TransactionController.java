package com.bytebridges.anytop.domain.transaction.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bytebridges.anytop.common.ServiceResponse;
import com.bytebridges.anytop.domain.transaction.dto.TopupRequest;
import com.bytebridges.anytop.domain.transaction.dto.TopupResponseDto;
import com.bytebridges.anytop.domain.transaction.service.TelcoService;
import com.bytebridges.anytop.domain.transaction.service.TransactionCommandService;
import com.bytebridges.anytop.domain.transaction.service.TransactionQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "ELOAD Transaction", description = "APIs for mobile topup transaction management and inquiry")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/eload")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionCommandService transactionCommandService;
	private final TransactionQueryService transactionQueryService;
	private final TelcoService telcoService;

	@Operation(summary = "Check Mobile Operator", description = "Detects Myanmar mobile operator type from the provided MSISDN number")
	@GetMapping("/checkTelco")
	public ServiceResponse<?> checkTelco(@RequestParam String msisdn) {
		return telcoService.checkTelco(msisdn);
	}

	@Operation(summary = "Create Topup Transaction", description = "Creates a new mobile topup transaction for the selected operator and phone number")
	@PostMapping("/topup")
	public TopupResponseDto createTopup(@RequestBody TopupRequest request) {

		return transactionCommandService.createTopup(request.getOperator(), request.getPhone(), request.getAmount());
	}

	@Operation(summary = "Get Transaction List", description = "Retrieves paginated transaction records filtered by date range")
	@GetMapping("/txns")
	public ServiceResponse<?> getTransactions(

			@RequestParam String fromDate, @RequestParam String toDate,

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "10") int size) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

		LocalDateTime startDate = LocalDate.parse(fromDate, formatter).atStartOfDay();

		LocalDateTime endDate = LocalDate.parse(toDate, formatter).atTime(23, 59, 59);

		return transactionQueryService.getTransactionsByCreatedAt(startDate, endDate, page, size);
	}
}