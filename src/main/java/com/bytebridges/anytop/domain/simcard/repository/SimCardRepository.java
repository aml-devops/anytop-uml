package com.bytebridges.anytop.domain.simcard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bytebridges.anytop.domain.simcard.entity.SimCard;
import com.bytebridges.anytop.domain.simcard.enums.SimStatus;
import com.bytebridges.anytop.domain.simcard.projection.SimCardProjection;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;

public interface SimCardRepository extends JpaRepository<SimCard, Long> {
	
	@Modifying
	@Transactional
	@Query(value = """
	    UPDATE sim_cards
	    SET balance = balance - :amount
	    WHERE id = :simId
	      AND balance >= :amount
	    """, nativeQuery = true)
	int deductBalance(Long simId, Integer amount);
	
	/**
	@Query("""
			    SELECT s
			    FROM SimCard s
			    WHERE s.isActive = true
			      AND s.status != 'DOWN'
			""")
	List<SimCard> findAllActiveSims();*/

	@Query("""
			    SELECT s.operator, SUM(s.balance)
			    FROM SimCard s
			    GROUP BY s.operator
			""")
	List<Object[]> getOperatorBalances();

	@Query("""
			    SELECT SUM(s.balance)
			    FROM SimCard s
			""")
	Long getGrandTotalBalance();

	@Query(value = """
			    SELECT id AS id,
			           operator AS operator,
			           sim_name AS simName,
			           is_active AS isActive,
			           balance AS balance
			    FROM sim_cards
			    WHERE operator = :operator
			""", nativeQuery = true)
	List<SimCardProjection> findSimCardsByOperator(String operator);

	@Query(value = """
			    SELECT id AS id,
			    	   msisdn As msisdn,
			           operator AS operator,
			           sim_name AS simName,
			           is_active AS isActive,
			           balance AS balance
			    FROM sim_cards
			""", nativeQuery = true)
	List<SimCardProjection> findSimCards();

	@Modifying
	@Transactional
	@Query(value = """
	        UPDATE sim_cards
	        SET is_active = :isActive,
	            status = :status
	        WHERE id = :id
	        """, nativeQuery = true)
	int updateSimStatus(
	        @Param("id") Long id,
	        @Param("isActive") Integer isActive,
	        @Param("status") String status
	);

	List<SimCard> findByIsActive(Boolean isActive);
}