package pers.lunex.utils.exceptions;

public class CannotConvertTypeException extends Exception {
    public CannotConvertTypeException() {
        super();
    }
    public CannotConvertTypeException(String message) {
        super(message);
    }
    public CannotConvertTypeException(String message, Throwable cause) {
        super(message, cause);
    }
    public CannotConvertTypeException(Throwable cause) {
        super(cause);
    }
}
