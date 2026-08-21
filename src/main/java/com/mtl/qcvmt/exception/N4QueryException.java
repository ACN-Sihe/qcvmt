package com.mtl.qcvmt.exception;

public class N4QueryException extends RuntimeException {

  public N4QueryException(String message) {
    super(message);
  }

  public N4QueryException(String message, Throwable cause) {
    super(message, cause);
  }
}
