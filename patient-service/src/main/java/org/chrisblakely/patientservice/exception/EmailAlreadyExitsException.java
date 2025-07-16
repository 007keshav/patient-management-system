package org.chrisblakely.patientservice.exception;

public class EmailAlreadyExitsException extends RuntimeException {
  public EmailAlreadyExitsException(String message) {
    super(message);
  }
}
