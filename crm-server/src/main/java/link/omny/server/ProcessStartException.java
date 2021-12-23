package link.omny.server;

public class ProcessStartException extends RuntimeException {

    private static final long serialVersionUID = -9027011010652162987L;

    public ProcessStartException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }

}
