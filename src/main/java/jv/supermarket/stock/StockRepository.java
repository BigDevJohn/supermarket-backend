package jv.supermarket.stock;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * StockRepository
 */
public interface StockRepository extends JpaRepository<Stock, Long> {

}
