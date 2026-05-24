package com.bytebridges.anytop.domain.batch;

import com.bytebridges.anytop.domain.transaction.entity.Transaction;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;
import com.bytebridges.anytop.domain.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelTopupService {

	private final TransactionRepository transactionRepository;

	@Transactional
	public int processExcel(MultipartFile file) throws Exception {

		int count = 0;

		List<Transaction> transactions = new ArrayList<>();

		try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {

			Sheet sheet = workbook.getSheetAt(0);

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

				Row row = sheet.getRow(i);

				if (row == null) {
					continue;
				}

				String operator = getCellValue(row.getCell(1)).trim();
				String phone = getCellValue(row.getCell(2)).trim();
				// remove Excel apostrophe if exists
				if (phone.startsWith("'")) {
				    phone = phone.substring(1);
				}
				String amountStr = getCellValue(row.getCell(3)).trim();

				if (phone.isBlank() || amountStr.isBlank()) {
					log.warn("Skipping row {} because phone or amount is empty", i);
					continue;
				}

				int amount;

				try {

					amount = Integer.parseInt(amountStr);

				} catch (Exception e) {

					log.warn("Invalid amount at row {} : {}", i, amountStr);
					continue;
				}

				Transaction txn = new Transaction();

				txn.setOperator(operator);
				txn.setPhoneNumber(phone);
				txn.setAmount(amount);

				txn.setStatus(TxnStatus.UPLOADED);

				txn.setMessageId(UUID.randomUUID().toString());

				txn.setCreatedAt(LocalDateTime.now());

				transactions.add(txn);

				count++;
			}
		}

		transactionRepository.saveAll(transactions);

		log.info("Excel upload completed. Total queued transactions: {}", count);

		return count;
	}

	private String getCellValue(Cell cell) {

		if (cell == null) {
			return "";
		}

		return switch (cell.getCellType()) {

		case STRING -> cell.getStringCellValue();

		case NUMERIC -> {

			double value = cell.getNumericCellValue();

			yield String.valueOf((long) value);
		}

		case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());

		default -> "";
		};
	}
}