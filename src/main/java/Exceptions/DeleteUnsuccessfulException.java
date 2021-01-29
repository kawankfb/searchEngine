package Exceptions;

public class DeleteUnsuccessfulException extends AppException {
    private static final String message = "The given url couldn't be deleted";
    private static final int errorCode = 1005;
    private static final int httpCode = 405;
    public DeleteUnsuccessfulException() {
        super(message, errorCode, httpCode);
    }
}
