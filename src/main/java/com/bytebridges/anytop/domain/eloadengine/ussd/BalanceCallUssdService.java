package com.bytebridges.anytop.domain.eloadengine.ussd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.bytebridges.anytop.config.EloadConfig;
import com.bytebridges.anytop.domain.eloadengine.external.UssdGatewayClient;
import com.bytebridges.anytop.domain.transaction.dto.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class BalanceCallUssdService {

	private final UssdGatewayClient client;
	private final EloadConfig config;

	// "05-03 17:00:54 09421251159 Your main balance is 503 Ks,
	// valid until 02/04/2027.Detail in MPT4U(*4040#). Dial *5555# for 100%
	// Cashback.";
	// 05-23 19:28:27 MSISDN:959421251159
	public Integer getMPTBalance(String port) {

		int balance = 0;
		String balUssd = "*124#";

		Message pre = client.sendUssd(config.getEndpoints().getUssdGateway(), port, balUssd);

		if (pre == null || pre.getResp() == null) {

			log.warn("Balance inquiry failed port={} reason=NULL_RESPONSE", port);

			return balance;
		}

		String response = pre.getResp();

		log.debug("MPT raw balance response port={} response={}", port, response);

		// Example:
		// Your main balance is 503 Ks
		// Your main balance is 12,500 Ks

		Pattern pattern = Pattern.compile("balance\\s+is\\s+([\\d,]+)\\s*Ks", Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(response);

		if (matcher.find()) {

			String balanceText = matcher.group(1).replace(",", "").trim();

			try {

				balance = Integer.parseInt(balanceText);

				log.info("MPT balance fetched port={} balance={} Ks", port, balance);

			} catch (NumberFormatException e) {

				log.error("Balance conversion failed port={} value={}", port, balanceText, e);
			}

		} else {

			log.warn("Balance parsing failed port={} response={}", port, response);
		}

		return balance;
	}

	// 9790175042 Balance 10007 Ks valid 15/04/2027.
	public Integer getAtomBalance(String port) {

		int balance = 0;
		String balUssd = "*124#";

		Message pre = client.sendUssd(config.getEndpoints().getUssdGateway(), port, balUssd);

		if (pre == null || pre.getResp() == null) {

			log.warn("ATOM balance inquiry failed port={} reason=NULL_RESPONSE", port);

			return balance;
		}

		String response = pre.getResp();

		log.debug("ATOM raw balance response port={} response={}", port, response);

		// Example:
		// 9790175042 Balance 10007 Ks valid 15/04/2027.

		Pattern pattern = Pattern.compile("Balance\\s+([\\d,]+)\\s*Ks", Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(response);

		if (matcher.find()) {

			String balanceText = matcher.group(1).replace(",", "").trim();

			try {

				balance = Integer.parseInt(balanceText);

				log.info("ATOM balance fetched port={} balance={} Ks", port, balance);

			} catch (NumberFormatException e) {

				log.error("ATOM balance conversion failed port={} value={}", port, balanceText, e);
			}

		} else {

			log.warn("ATOM balance parsing failed port={} response={}", port, response);
		}

		return balance;
	}

	// 9962866776\r\nBal: 2517 Ks, Valid:19/11/2026. \r\n1. 666MB=2199Ks\r\n2. 160
	// U9 Mins=1999Ks\r\n3. 105 AllCall Mins=1999Ks\r\n4. Ayan Ayan Tan\r\n5.
	// Combo\r\n
	public Integer getU9Balance(String port) {

		int balance = 0;
		String balUssd = "*124#";

		Message pre = client.sendUssd(config.getEndpoints().getUssdGateway(), port, balUssd);

		if (pre == null || pre.getResp() == null) {

			log.warn("U9 balance inquiry failed port={} reason=NULL_RESPONSE", port);

			return balance;
		}

		String response = pre.getResp();

		log.debug("U9 raw balance response port={} response={}", port, response);

		// Example:
		// Bal: 2517 Ks, Valid:19/11/2026.

		Pattern pattern = Pattern.compile("Bal:\\s*([\\d,]+)\\s*Ks", Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(response);

		if (matcher.find()) {

			String balanceText = matcher.group(1).replace(",", "").trim();

			try {

				balance = Integer.parseInt(balanceText);

				log.info("U9 balance fetched port={} balance={} Ks", port, balance);

			} catch (NumberFormatException e) {

				log.error("U9 balance conversion failed port={} value={}", port, balanceText, e);
			}

		} else {

			log.warn("U9 balance parsing failed port={} response={}", port, response);
		}

		return balance;
	}

	// 09690577895- 0Ks, exp 04/12/2026. Dial *613# for 999ks=454MB+11(Diamond)
	public Integer getMytelBalance(String port) {

		int balance = 0;
		String balUssd = "*124#";

		Message pre = client.sendUssd(config.getEndpoints().getUssdGateway(), port, balUssd);

		if (pre == null || pre.getResp() == null) {

			log.warn("MYTEL balance inquiry failed port={} reason=NULL_RESPONSE", port);

			return balance;
		}

		String response = pre.getResp();

		log.debug("MYTEL raw balance response port={} response={}", port, response);

		// Example:
		// 09690577895- 0Ks, exp 04/12/2026.

		Pattern pattern = Pattern.compile("-\\s*([\\d,]+)\\s*Ks", Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(response);

		if (matcher.find()) {

			String balanceText = matcher.group(1).replace(",", "").trim();

			try {

				balance = Integer.parseInt(balanceText);

				log.info("MYTEL balance fetched port={} balance={} Ks", port, balance);

			} catch (NumberFormatException e) {

				log.error("MYTEL balance conversion failed port={} value={}", port, balanceText, e);
			}

		} else {

			log.warn("MYTEL balance parsing failed port={} response={}", port, response);
		}

		return balance;
	}

}
