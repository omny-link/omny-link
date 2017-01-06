package link.omny.catalog;

public class CatalogException extends RuntimeException {

    private static final long serialVersionUID = 8596935185538284707L;

    public CatalogException() {
        super();
    }

    public CatalogException(String message, Throwable cause) {
        super(message, cause);
    }

    public CatalogException(String message) {
        super(message);
    }

    public CatalogException(Throwable cause) {
        super(cause);
    }

}
