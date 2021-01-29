package Exceptions;

public class UrlNotExistsException extends AppException {
    private static final String message = "The given URL does not exists";
    private static final int errorCode = 1004;
    private static final int httpCode = 404;

    public UrlNotExistsException() {
        super(message, errorCode, httpCode);
    }
}
