package exception;

public class IllegalDateException extends Exception {
    public IllegalDateException() {
        super("Given date is invalid!");
    }
}
