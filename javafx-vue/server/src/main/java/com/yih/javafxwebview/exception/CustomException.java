package com.yih.javafxwebview.exception;

public class CustomException extends Exception {

    public CustomException() {
        super();
    }

    public CustomException(String msg) {
        super(msg);
    }

    public CustomException(Throwable t) {
        super(t);
    }

    public CustomException(String msg, Throwable t) {
        super(msg, t);
    }

}
