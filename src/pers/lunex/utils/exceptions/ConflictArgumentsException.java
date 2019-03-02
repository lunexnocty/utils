package pers.lunex.utils.exceptions;

public class ConflictArgumentsException extends Exception {
    public ConflictArgumentsException() {
        super();
    }
    public ConflictArgumentsException(String message) {
        super(message);
    }
    public ConflictArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }
    public ConflictArgumentsException(Throwable cause) {
        super(cause);
    }
}
