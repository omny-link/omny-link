package link.omny.custmgmt;

public class CustMgmtException extends RuntimeException {

    private static final long serialVersionUID = 8596935185538284707L;

    public CustMgmtException() {
        super();
    }

    public CustMgmtException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustMgmtException(String message) {
        super(message);
    }

    public CustMgmtException(Throwable cause) {
        super(cause);
    }

}
