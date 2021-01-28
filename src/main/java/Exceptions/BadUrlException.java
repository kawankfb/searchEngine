package Exceptions;

public class BadUrlException extends AppException {
    private static final String message = "Bad url was given";
    private static final int errorCode = 1001;
    private static final int httpCode = 406;

    public BadUrlException() {
        super(message, errorCode, httpCode);
    }
}
