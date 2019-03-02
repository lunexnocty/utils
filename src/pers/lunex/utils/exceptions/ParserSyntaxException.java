package pers.lunex.utils.exceptions;

public class ParserSyntaxException extends Exception {
    public ParserSyntaxException() {
        super();
    }
    public ParserSyntaxException(String message) {
        super(message);
    }
    public ParserSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
    public ParserSyntaxException(Throwable cause) {
        super(cause);
    }
}
