package com.bytebridges.anytop.domain.simcard.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;

import com.bytebridges.anytop.domain.simcard.entity.SimCard;
import com.bytebridges.anytop.domain.simcard.repository.SimCardRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe SIM pool manager.
 *
 * Manages SIM allocation and release for operator-based GSM processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimPoolManager {

	private final SimCardRepository simCardRepository;

	// operator -> sim queue
	private final Map<String, BlockingQueue<SimCard>> pools = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		List<SimCard> sims = simCardRepository.findByIsActive(true);

		for (SimCard sim : sims) {
			// Normalize input string to uppercase to avoid mismatch
			String operatorKey = sim.getOperator().toUpperCase();
			// Dynamically create the queue if it doesn't exist yet
			pools.computeIfAbsent(operatorKey, k -> new LinkedBlockingQueue<>()).offer(sim);
			log.info("SIM pool loaded operator={} simId={} port={}", operatorKey, sim.getId(), sim.getSimName());
		}
		log.info("SIM pool initialized operators={}", pools.size());
	}

	// =========================
	// ACQUIRE SIM
	// =========================
	public SimCard acquire(String operator) {
		if (operator == null) {
			throw new IllegalArgumentException("Operator cannot be null");
		}

		String operatorKey = operator.toUpperCase();
		BlockingQueue<SimCard> queue = pools.get(operatorKey);
		if (queue == null) {
			throw new IllegalArgumentException("Unsupported operator: " + operator);
		}

		try {
			// Fix: Removed unnecessary null check since take() blocks until an item is present
			SimCard sim = queue.take();
			log.info("SIM acquired operator={} simId={} port={}", operatorKey, sim.getId(), sim.getSimName());
			return sim;
		} catch (InterruptedException ex) {
			log.error("Thread interrupted while waiting for SIM operator={}", operatorKey, ex);
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted while waiting for SIM from pool", ex);
		}
	}

	// =========================
	// RELEASE SIM
	// =========================
	public void release(String operator, SimCard sim) {
		if (operator == null || sim == null) {
			return;
		}

		String operatorKey = operator.toUpperCase();
		BlockingQueue<SimCard> queue = pools.get(operatorKey);
		if (queue == null) {
			log.warn("SIM release skipped operator={} reason=POOL_NOT_FOUND", operatorKey);
			return;
		}

		// Using offer is fine, but in an bounded queue or robust system,
		// put() or checking return value ensures resources aren't lost.
		boolean added = queue.offer(sim);
		if (added) {
			log.info("SIM released operator={} simId={} port={}", operatorKey, sim.getId(), sim.getSimName());
			log.info("-----------------------------------------------");
		} else {
			log.error("Failed to release SIM pool full? operator={} simId={}", operatorKey, sim.getId());
		}
	}

	// =========================
	// AVAILABLE COUNT
	// =========================
	public int available(String operator) {
		if (operator == null) {
			return 0;
		}

		BlockingQueue<SimCard> queue = pools.get(operator.toUpperCase());
		return queue == null ? 0 : queue.size();
	}
}