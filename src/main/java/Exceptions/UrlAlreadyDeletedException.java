package Exceptions;

public class UrlAlreadyDeletedException extends AppException {
    private static final String message = "The given URL is Already deleted";
    private static final int errorCode = 1003;
    private static final int httpCode = 410;

    public UrlAlreadyDeletedException() {
        super(message, errorCode, httpCode);
    }
}
