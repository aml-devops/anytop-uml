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

		String normalized = normalizeMsisdn(msisdn);

		if (normalized == null) {
			return Operator.UNKNOWN;
		}

		/*
		 * Extract operator code after 09
		 * Example:
		 * 09981234567 -> 98
		 * 09791234567 -> 79
		 */
		String code = normalized.substring(2, 4);

		/*
		 * U9 / Ooredoo
		 * 94 / 95 / 96 / 97 / 98
		 */
		if (isIn(code, "94", "95", "96", "97", "98")) {
			return Operator.U9;
		}

		/*
		 * ATOM
		 * 74 / 75 / 76 / 77 / 78 / 79
		 */
		if (isIn(code, "74", "75", "76", "77", "78", "79")) {
			return Operator.ATOM;
		}

		/*
		 * MYTEL
		 * 66 / 67 / 68 / 69
		 */
		if (isIn(code, "66", "67", "68", "69")) {
			return Operator.MYTEL;
		}

		/*
		 * MPT
		 * 20-26
		 * 40 / 43 / 44 / 45
		 * 50-56
		 * 88 / 89
		 */
		if (isIn(code,
				"20", "21", "22", "23", "24", "25", "26",
				"40", "43", "44", "45",
				"50", "51", "52", "53", "54", "55", "56",
				"88", "89")) {

			return Operator.MPT;
		}

		return Operator.UNKNOWN;
	}

	private String normalizeMsisdn(String msisdn) {

		if (msisdn == null || msisdn.isBlank()) {
			return null;
		}

		// Remove non-digit characters
		msisdn = msisdn.replaceAll("\\D", "");

		/*
		 * Normalize:
		 * 959xxxxxxxxx -> 09xxxxxxxxx
		 */
		if (msisdn.startsWith("959")) {
			msisdn = "0" + msisdn.substring(2);
		}

		/*
		 * Validate Myanmar mobile number
		 */
		if (!msisdn.startsWith("09")) {
			return null;
		}

		if (msisdn.length() < 9 || msisdn.length() > 11) {
			return null;
		}

		return msisdn;
	}

	private boolean isIn(String value, String... values) {

		for (String item : values) {
			if (item.equals(value)) {
				return true;
			}
		}

		return false;
	}

	public ServiceResponse<?> checkTelco(String msisdn) {

		try {

			Operator operator = detectTelco(msisdn);

			Map<String, Object> response = new HashMap<>();
			response.put("msisdn", msisdn);
			response.put("operator", operator);

			log.info("TELCO_DETECTED msisdn={} operator={}", msisdn, operator);

			return ServiceResponse.success(response, "Telco detected successfully");

		} catch (Exception e) {

			log.error("TELCO_DETECTION_FAILED msisdn={}", msisdn, e);

			return ServiceResponse.error(500, "Failed to detect telco");
		}
	}
}