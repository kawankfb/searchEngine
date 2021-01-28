package Exceptions;

public class UrlAlreadyExistsException extends AppException {
    private static final String message = "The given URL is Already added";
    private static final int errorCode = 1002;
    private static final int httpCode = 409;

    public UrlAlreadyExistsException() {
        super(message, errorCode, httpCode);
    }
}
