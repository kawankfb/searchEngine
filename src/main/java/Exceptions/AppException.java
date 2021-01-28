package Exceptions;

import org.json.simple.JSONObject;

public class AppException extends Exception {
    public int getErrorCode() {
        return errorCode;
    }

    public int getHttpCode() {
        return httpCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    private int errorCode;
    private int httpCode;
    private String message;

    public AppException(String message, int errorCode, int httpCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpCode = httpCode;
        this.message = message;
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("error",this.message);
        jsonObject.put("error code",errorCode);
        this.message=jsonObject.toJSONString();
    }
}
