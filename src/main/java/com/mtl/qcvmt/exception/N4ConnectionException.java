package com.mtl.qcvmt.exception;

public class N4ConnectionException extends RuntimeException {

  public N4ConnectionException(String message) {
    super(message);
  }

  public N4ConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
