package jv.supermarket.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * StockRepository
 */
public interface StockRepository extends JpaRepository<Stock, Long> {

@Modifying
@Query("UPDATE Stock s SET s.quantity = s.quantity - :quantity WHERE s.id = :productId AND s.quantity >= :quantity")
int decrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
