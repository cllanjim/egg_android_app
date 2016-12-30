package com.lingyang.sdk.exception;

/**
 * LingYang  Exception
 */
public class LYException extends Exception {
    private String mMessage;
    private int mCode;

    public LYException() {
        mMessage = "An unknown error occurred";
        mCode = 0;
    }

    public LYException(Exception e) {
        mMessage = e.getMessage();
    }

    public LYException(int code, String message) {
        mMessage = message;
        mCode = code;
    }

    public String getMessage() {
        return mMessage;
    }

    @Override
    public String toString() {
        return "message=" + mMessage + ",Code=" + mCode;
    }

    public int getCode() {
        return mCode;
    }
}
