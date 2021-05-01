package exception;

public class InvalidDateFormatException extends Exception {
    public InvalidDateFormatException(){
        super("Date format is invalid");
    }
}
