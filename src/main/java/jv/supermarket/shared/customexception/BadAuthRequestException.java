package jv.supermarket.shared.customexception;

public class BadAuthRequestException extends RuntimeException {

    public BadAuthRequestException(String message) {
        super(message);
    }

}
