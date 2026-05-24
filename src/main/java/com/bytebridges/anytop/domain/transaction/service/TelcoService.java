package com.bytebridges.anytop.domain.transaction.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bytebridges.anytop.common.ServiceResponse;
import com.bytebridges.anytop.domain.transaction.enums.Operator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelcoService {

	public Operator detectTelco(String msisdn) {

		if (msisdn == null || msisdn.isBlank()) {
			return Operator.UNKNOWN;
		}

		// Remove non-digit characters
		msisdn = msisdn.replaceAll("\\D", "");

		// Normalize:
		// 959xxxxxxxxx -> 09xxxxxxxxx
		if (msisdn.startsWith("959")) {
			msisdn = "0" + msisdn.substring(2);
		}

		// Validate Myanmar format
		if (!msisdn.startsWith("09") || msisdn.length() < 9) {
			return Operator.UNKNOWN;
		}

		// Extract prefix
		String prefix4 = msisdn.length() >= 4 ? msisdn.substring(0, 4) : msisdn;

		String prefix5 = msisdn.length() >= 5 ? msisdn.substring(0, 5) : msisdn;

		/*
		 * MYTEL 0960 - 0965
		 */
		if (prefix4.matches("096[0-5]")) {
			return Operator.MYTEL;
		}

		/*
		 * ATOM (formerly Telenor) 0966 - 0969 0975 - 0979
		 */
		if (prefix4.matches("096[6-9]") || prefix4.matches("097[5-9]")) {

			return Operator.ATOM;
		}

		/*
		 * U9 / Ooredoo 0995 - 0999
		 */
		if (prefix4.matches("099[5-9]")) {
			return Operator.U9;
		}

		/*
		 * Default MPT
		 */
		return Operator.MPT;
	}

	public ServiceResponse<?> checkTelco(String msisdn) {

		try {

			Operator operator = detectTelco(msisdn);

			Map<String, Object> response = new HashMap<>();
			response.put("msisdn", msisdn);
			response.put("operator", operator);

			log.info("Telco detected successfully | msisdn={} | operator={}", msisdn, operator);

			return ServiceResponse.success(response, "Telco detected successfully");

		} catch (Exception e) {

			log.error("Failed to detect telco | msisdn={}", msisdn, e);

			return ServiceResponse.error(500, "Failed to detect telco");
		}
	}
}
