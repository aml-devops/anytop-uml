package com.bytebridges.anytop.domain.transaction.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bytebridges.anytop.domain.transaction.dto.OperatorTransactionChartDto;
import com.bytebridges.anytop.domain.transaction.entity.Transaction;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	Page<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

	@Query("""
			    SELECT t
			    FROM Transaction t
			    WHERE t.status = 'QUEUED'
			    ORDER BY t.createdAt ASC
			""")
	List<Transaction> findQueuedTransactions(Pageable pageable);

	Optional<Transaction> findByMessageId(String messageId);

	/**
	 * Use only this in your own system
	 * 
	 * @Modifying
	 * @Transactional
	 * @Query(value = """ UPDATE transactions SET status = 'PROCESSING' WHERE id =
	 *              :txnId AND status = 'QUEUED' """, nativeQuery = true) int
	 *              markProcessing(@Param("txnId") Long txnId);
	 */

	// Remove this later
	@Modifying
	@Transactional
	@Query(value = """
			UPDATE transactions
			SET status = 'PROCESSING'
			WHERE id = :txnId
			  AND status IN ('QUEUED', 'UPLOADED')
			""", nativeQuery = true)
	int markProcessing(@Param("txnId") Long txnId);

	@Query(value = """
			SELECT *
			FROM transactions
			WHERE status = 'UPLOADED'
			ORDER BY id
			LIMIT :limit
			""", nativeQuery = true)
	List<Transaction> findBatchForPublishing(int limit);

	@Modifying
	@Transactional
	@Query(value = """
			UPDATE transactions
			SET status = :status,
			    sim_id = :simId,
			    sim_name = :simName
			WHERE id = :txnId
			""", nativeQuery = true)
	int completeTransaction(Long txnId, String status, Long simId, String simName);

	@Modifying
	@Transactional
	@Query(value = """
			    UPDATE transactions
			    SET status = 'FAILED_SYSTEM'
			    WHERE id = :txnId
			    AND status = 'PROCESSING'
			""", nativeQuery = true)
	int markSystemFailed(Long txnId);

	@Query(value = """
			SELECT
			    t.operator AS operator,
			    COUNT(*) AS totalTransactions
			FROM transactions t
			GROUP BY t.operator
			ORDER BY COUNT(*) DESC
			""", nativeQuery = true)
	List<OperatorTransactionChartDto> getTransactionCountPerOperator();

	/**
	 * 2. GENERAL STATUS UPDATE (NATIVE SQL) Spring Data JPA will automatically
	 * convert the TxnStatus Enum name() into a string parameter for the SQL query.
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE transactions SET status = :status WHERE id = :id", nativeQuery = true)
	int updateStatus(@Param("id") Long id, @Param("status") String status);
}
