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
public class MsisdnCallUssdService {

	private final UssdGatewayClient client;
	private final EloadConfig config;

	// 05-23 19:28:27 MSISDN:959421251159
	public String getMptMsisdn(String port) {

		String msisdn = "";
		String msisdnUssd = "*88#";

		Message pre = client.sendUssd(config.getEndpoints().getUssdGateway(), port, msisdnUssd);

		if (pre == null || pre.getResp() == null) {

			log.warn("USSD_MSISDN_REQUEST_FAILED telco=MPT port={} reason=NULL_RESPONSE", port);

			return msisdn;
		}

		String response = pre.getResp();

		log.debug("USSD_MSISDN_RAW_RESPONSE telco=MPT port={} response={}", port, response);

		Pattern pattern = Pattern.compile("MSISDN:(\\d+)", Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(response);

		if (matcher.find()) {

			msisdn = matcher.group(1);

			log.info("USSD_MSISDN_EXTRACTED telco=MPT port={} msisdn={}", port, msisdn);

		} else {

			log.warn("USSD_MSISDN_PARSE_FAILED telco=MPT port={} response={}", port, response);
		}

		return msisdn;
	}

	// 05-23 12:56:25 MSISDN:959752059235
	public String getAtomMsisdn(String port) {

		String msisdn = "";
		String msisdnUssd = "*97#";

		Message pre = client.sendUssd(config.getEndpoints().getUssdGateway(), port, msisdnUssd);

		if (pre == null || pre.getResp() == null) {

			log.warn("USSD_MSISDN_REQUEST_FAILED telco=ATOM port={} reason=NULL_RESPONSE", port);

			return msisdn;
		}

		String response = pre.getResp();

		log.debug("USSD_MSISDN_RAW_RESPONSE telco=ATOM port={} response={}", port, response);

		Pattern pattern = Pattern.compile("MSISDN:(\\d+)", Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(response);

		if (matcher.find()) {

			msisdn = matcher.group(1);

			log.info("USSD_MSISDN_EXTRACTED telco=ATOM port={} msisdn={}", port, msisdn);

		} else {

			log.warn("USSD_MSISDN_PARSE_FAILED telco=ATOM port={} response={}", port, response);
		}

		return msisdn;
	}

	// 05-23 22:56:49 My number +959953230330. Dial *140# to buy Data Packs!
	public String getU9Msisdn(String port) {

		String msisdn = "";
		String msisdnUssd = "*133*6*2#";

		Message pre = client.sendUssd(config.getEndpoints().getUssdGateway(), port, msisdnUssd);

		if (pre == null || pre.getResp() == null) {

			log.warn("USSD_MSISDN_REQUEST_FAILED telco=U9 port={} reason=NULL_RESPONSE", port);

			return msisdn;
		}

		String response = pre.getResp();

		log.debug("USSD_MSISDN_RAW_RESPONSE telco=U9 port={} response={}", port, response);

		Pattern pattern = Pattern.compile("\\+?(95\\d+)");

		Matcher matcher = pattern.matcher(response);

		if (matcher.find()) {

			msisdn = matcher.group(1);

			log.info("USSD_MSISDN_EXTRACTED telco=U9 port={} msisdn={}", port, msisdn);

		} else {

			log.warn("USSD_MSISDN_PARSE_FAILED telco=U9 port={} response={}", port, response);
		}

		return msisdn;
	}

	// 05-23 22:51:07 You current on MDMS01 and your phone is959685637673
	public String getMytelMsisdn(String port) {

		String msisdn = "";
		String msisdnUssd = "*88#";

		Message pre = client.sendUssd(config.getEndpoints().getUssdGateway(), port, msisdnUssd);

		if (pre == null || pre.getResp() == null) {

			log.warn("USSD_MSISDN_REQUEST_FAILED telco=MYTEL port={} reason=NULL_RESPONSE", port);

			return msisdn;
		}

		String response = pre.getResp();

		log.debug("USSD_MSISDN_RAW_RESPONSE telco=MYTEL port={} response={}", port, response);

		Pattern pattern = Pattern.compile("(95\\d+)");

		Matcher matcher = pattern.matcher(response);

		if (matcher.find()) {

			msisdn = matcher.group(1);

			log.info("USSD_MSISDN_EXTRACTED telco=MYTEL port={} msisdn={}", port, msisdn);

		} else {

			log.warn("USSD_MSISDN_PARSE_FAILED telco=MYTEL port={} response={}", port, response);
		}

		return msisdn;
	}
}