package com.bytebridges.anytop.domain.simcard.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bytebridges.anytop.common.ServiceResponse;
import com.bytebridges.anytop.domain.simcard.service.SimCardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "SIM Card Management", description = "APIs for SIM card management, balance inquiry, and SIM status control")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/sims")
@RequiredArgsConstructor
public class SimCardController {

	private final SimCardService simCardService;

	@Operation(summary = "Get Operator Balances", description = "Retrieves total balance summary grouped by mobile operators")
	@GetMapping("/balances")
	public ServiceResponse<?> getOperatorBalances() {

		return simCardService.getOperatorBalances();
	}

	@Operation(summary = "Get SIM Cards By Operator", description = "Retrieves SIM card list filtered by operator name")
	@GetMapping("/operator")
	public ServiceResponse<?> getSimCardsByOperator(@RequestParam String operator) {
		return simCardService.getSimCardsByOperator(operator);
	}

	@Operation(summary = "Get All SIM Cards", description = "Retrieves all registered SIM cards")
	@GetMapping
	public ServiceResponse<?> getSimCards() {
		return simCardService.getSimCards();
	}

	@Operation(summary = "Update SIM Status", description = "Updates SIM card active status using SIM card ID")
	@PutMapping("/{id}/status")
	public ServiceResponse<?> updateSimStatus(@PathVariable Long id, @RequestParam Integer isActive) {
		return simCardService.updateSimStatus(id, isActive);
	}
}
