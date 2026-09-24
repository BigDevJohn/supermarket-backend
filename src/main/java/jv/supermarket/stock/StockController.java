package jv.supermarket.stock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/supermarket/stock/{stockId}")
public class StockController {

    private final StockService stockService;

    StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public StockDTO getProductStock(@PathVariable Long stockId) {
        return stockService.getStockById(stockId);
    }

    @PutMapping("/entries")
    public StockDTO addProductEntries(@RequestBody StockDTO stock, @PathVariable Long stockId) {
        return stockService.stockEntry(stockId, stock.quantity());
    }

    @PutMapping("/exits")
    public StockDTO subtractProductStock(@RequestBody StockDTO stock, @PathVariable Long stockId) {
        return stockService.stockExit(stockId, stock.quantity());
    }

}