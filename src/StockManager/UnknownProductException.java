package stockManager;

import java.lang.Throwable;
import java.lang.Exception;


public class UnknownProductException extends Exception {
    public UnknownProductException() { super(); }
    public UnknownProductException(String message) { super(message); }
    public UnknownProductException(String message, 
                                 Throwable cause) { super(message, cause); }
    public UnknownProductException(Throwable cause) { super(cause); }

    public static final long serialVersionUID = 124L;
}