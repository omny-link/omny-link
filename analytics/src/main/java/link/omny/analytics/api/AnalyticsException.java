package link.omny.analytics.api;

public class AnalyticsException extends RuntimeException {

    private static final long serialVersionUID = 7569537998617199463L;

    public AnalyticsException() {
        super();
    }

    public AnalyticsException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public AnalyticsException(String arg0) {
        super(arg0);
    }

    public AnalyticsException(Throwable arg0) {
        super(arg0);
    }

}
