package com.bytebridges.anytop.domain.transaction.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.bytebridges.anytop.common.ServiceResponse;
import com.bytebridges.anytop.domain.transaction.dto.PaginationDto;
import com.bytebridges.anytop.domain.transaction.dto.TransactionPageResponseDto;
import com.bytebridges.anytop.domain.transaction.dto.TransactionResponseDto;
import com.bytebridges.anytop.domain.transaction.entity.Transaction;
import com.bytebridges.anytop.domain.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionQueryService {
	private final TransactionRepository transactionRepository;

	public ServiceResponse<?> getTransactionsByCreatedAt(LocalDateTime startDate, LocalDateTime endDate, int page,
			int size) {

		try {

			Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

			Page<Transaction> transactionPage = transactionRepository.findByCreatedAtBetween(startDate, endDate,
					pageable);

			List<TransactionResponseDto> items = transactionPage.getContent().stream().map(this::mapToDto).toList();

			PaginationDto pagination = new PaginationDto(transactionPage.getNumber(), transactionPage.getSize(),
					transactionPage.getTotalElements(), transactionPage.getTotalPages(), transactionPage.isFirst(),
					transactionPage.isLast());

			TransactionPageResponseDto response = new TransactionPageResponseDto(items, pagination);

			log.info("Transactions fetched successfully | page={} | size={} | totalElements={}", page, size,
					transactionPage.getTotalElements());

			return ServiceResponse.success(response, "Transactions fetched successfully");

		} catch (Exception e) {

			log.error("Failed to fetch transactions", e);

			return ServiceResponse.error(500, "Failed to fetch transactions");
		}
	}

	private TransactionResponseDto mapToDto(Transaction transaction) {

		return new TransactionResponseDto(transaction.getId(), transaction.getPhoneNumber(), transaction.getAmount(),
				transaction.getOperator(), transaction.getStatus().name(), transaction.getSimId(), transaction.getMessageId(),
				transaction.getCreatedAt());
	}
}