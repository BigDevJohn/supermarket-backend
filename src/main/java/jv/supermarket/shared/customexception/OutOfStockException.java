package jv.supermarket.shared.customexception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String message){
        super(message);
    }
}