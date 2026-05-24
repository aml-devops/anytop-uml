package com.bytebridges.anytop.domain.transaction.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bytebridges.anytop.common.ServiceResponse;
import com.bytebridges.anytop.domain.transaction.dto.OperatorTransactionChartDto;
import com.bytebridges.anytop.domain.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

	private final TransactionRepository transactionRepository;

	public ServiceResponse<?> getTransactionCountPerOperator() {

		try {

			List<OperatorTransactionChartDto> items = transactionRepository.getTransactionCountPerOperator();

			log.info("Transaction count per operator fetched successfully | totalOperators={}", items.size());

			return ServiceResponse.success(items, "Transaction count per operator fetched successfully");

		} catch (Exception e) {

			log.error("Failed to fetch transaction count per operator", e);

			return ServiceResponse.error(500, "Failed to fetch transaction count per operator");
		}
	}

}
