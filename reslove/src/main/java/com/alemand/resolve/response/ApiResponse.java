package com.alemand.resolve.response;


/**
 * Created by guocai.zhang on 16/5/28.
 */
public class ApiResponse {

    public static final ApiResponse SUC = new ApiResponse(ReturnCode.CODE_SUCCESS, "Success", null);
    public static final ApiResponse FAIL = new ApiResponse();

    private int status;
    private String info;
    private Object resultObject;

    public ApiResponse() {
        this.status = ReturnCode.CODE_FAIL;
    }

    public ApiResponse(int status, String info, Object resultObject) {
        this.status = status;
        this.info = info;
        this.resultObject = resultObject;
    }


    public static ApiResponse immediateOf(int status) {
        return new ApiResponse(status, "", null);
    }

    public static ApiResponse immediateOf(int status, String info) {
        return new ApiResponse(status, info, null);
    }

    public static ApiResponse failOf(Integer status, String info) {
        if (status == null) {
            status = ReturnCode.CODE_FAIL;
        }
        return new ApiResponse(status, info, null);
    }

    public static ApiResponse immediateOf(int status, String info, Object data) {
        return new ApiResponse(status, info, data);
    }

    public static ApiResponse successOf(Object data) {
        return immediateOf(ReturnCode.CODE_SUCCESS, "success", data);
    }

    public Object getResultObject() {
        return resultObject;
    }

    public void setResultObject(Object resultObject) {
        this.resultObject = resultObject;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean hasError() {
        return getStatus() != ReturnCode.CODE_SUCCESS;
    }

}
