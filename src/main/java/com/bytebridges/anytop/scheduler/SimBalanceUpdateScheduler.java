package com.bytebridges.anytop.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bytebridges.anytop.domain.simcard.service.SimCardService;

import java.time.LocalDateTime;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimBalanceUpdateScheduler {

	private final SimCardService simCardService;

	// second minute hour day month weekday
	// 0 0 2 * * *
	// One time per hour
	// @Scheduled(cron = "0 0 * * * *")
	// To run at 2:00 AM every day
	//@Scheduled(cron = "0 0 2 * * *")
	// @Scheduled(cron = "0 * * * * *")

	public void refreshBalances() {

		LocalDateTime startTime = LocalDateTime.now();

		log.info("SIM balance refresh started at {}", startTime);

		try {

			simCardService.refreshActiveSimBalances();

		} finally {

			LocalDateTime endTime = LocalDateTime.now();

			Duration duration = Duration.between(startTime, endTime);

			log.info("SIM balance refresh completed at {}", endTime);
			log.info("SIM balance refresh duration: {} seconds ({} ms)", duration.getSeconds(), duration.toMillis());
		}
	}
}
