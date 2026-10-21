package jv.supermarket.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jv.supermarket.product.Product;
import jv.supermarket.shared.customexception.OutOfStockException;
import jv.supermarket.shared.customexception.ResourceNotFoundException;

@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final StockRepository stockRepository;

    StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public StockDTO getStockById(Long id) {
        log.info("[getStockById] Fetching stock for ID: {}", id);
        final Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[getStockById] Stock with id: {} not found", id);
                    return new ResourceNotFoundException("Stock with id: " + id + " not found");
                });
        return new StockDTO(stock.getQuantity());
    }

    public Stock buildStock(Integer quantity, Product product) {
        log.info("[buildStock] Building stock with quantity: {} for product ID: {}", quantity,
                product != null ? product.getId() : null);
        if (quantity == null || quantity < 0) {
            log.warn("[buildStock] Initial stock quantity cannot be negative or null: {}", quantity);
            throw new IllegalArgumentException("Initial stock quantity cannot be negative");
        }
        return new Stock(quantity, product);
    }

    public StockDTO stockEntry(Long productId, Integer quantity) {
        log.info("[stockEntry] Adding quantity: {} to stock for product ID: {}", quantity, productId);
        Stock stock = stockRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("[stockEntry] Stock with product id: {} not found", productId);
                    return new ResourceNotFoundException("Stock with product id: " + productId + " not found");
                });

        stock.setQuantity(stock.getQuantity() + quantity);
        stockRepository.save(stock);
        log.info("[stockEntry] Stock updated successfully for product ID: {}. New quantity: {}", productId, stock.getQuantity());

        return new StockDTO(stock.getQuantity());
    }

    public StockDTO stockExit(Long productId, Integer quantity) {
        log.info("[stockExit] Removing quantity: {} from stock for product ID: {}", quantity, productId);
        Stock stock = stockRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("[stockExit] Stock with product id: {} not found", productId);
                    return new ResourceNotFoundException("Stock with product id: " + productId + " not found");
                });
        if (stock.getQuantity() < quantity) {
            log.warn("[stockExit] Insufficient stock for product ID: {}. Available: {}, Requested: {}", productId, stock.getQuantity(), quantity);
            throw new OutOfStockException("Insufficient stock quantity");
        }
        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepository.save(stock);
        log.info("[stockExit] Stock updated successfully for product ID: {}. New quantity: {}", productId, stock.getQuantity());
        return new StockDTO(stock.getQuantity());
    }

    public boolean isStockAvailable(Integer quantity, Long productId) {
        log.info("[isStockAvailable] Checking if stock quantity {} is available for product ID: {}", quantity, productId);
        Stock stock = stockRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("[isStockAvailable] Stock with product id: {} not found", productId);
                    return new ResourceNotFoundException("Stock with product id: " + productId + " not found");
                });
        boolean available = quantity <= stock.getQuantity();
        log.info("[isStockAvailable] Stock availability for product ID {}: {}", productId, available);
        return available;
    }
}
