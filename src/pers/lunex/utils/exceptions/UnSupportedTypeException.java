package pers.lunex.utils.exceptions;

public class UnSupportedTypeException extends Exception {
    public UnSupportedTypeException() {
        super();
    }
    public UnSupportedTypeException(String message) {
        super(message);
    }
    public UnSupportedTypeException(String message, Throwable cause) {
        super(message, cause);
    }
    public UnSupportedTypeException(Throwable cause) {
        super(cause);
    }
}
