package in.kuros.jfirebase.exception;

public class PersistenceException extends RuntimeException {

    public PersistenceException(final String message) {
        super(message);
    }

    public PersistenceException(final Throwable cause) {
        super(cause);
    }

    public PersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
