package com.tianqizhang.webapp.Utils;

public class RestResponse {

    private final static int STATUS_SUCCESS = 200;

    private final static int STATUS_BAD_REQUEST = 400;
    private final static int STATUS_UNAUTHORIZED= 401;
    private final static int STATUS_FORBIDDEN = 403;

    private int status;
    private String message;
    private Object data;

    public RestResponse(int code, String message, Object data) {
        this.status = code;
        this.message = message;
        this.data = data;
    }

    public static RestResponse buildSuccess(Object data) {
        return new RestResponse(STATUS_SUCCESS, "success", data);
    }

    public static RestResponse buildBadRequest(Object data) {
        return new RestResponse(STATUS_BAD_REQUEST, "Bad Request", data);
    }

    public static RestResponse buildUnauthorized(Object data) {
        return new RestResponse(STATUS_UNAUTHORIZED, "Unauthorized", data);
    }

    public static RestResponse buildForbidden(Object data) {
        return new RestResponse(STATUS_FORBIDDEN, "Forbidden", data);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
