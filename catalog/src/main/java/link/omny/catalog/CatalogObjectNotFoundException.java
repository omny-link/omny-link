package link.omny.catalog;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CatalogObjectNotFoundException extends CatalogException {

    private static final long serialVersionUID = -5639258797169164350L;

    private Class<?> type;

    private Object id;

    public CatalogObjectNotFoundException() {
        super();
    }

    public CatalogObjectNotFoundException(String message, Class<?> type,
            Object id) {
        super(message);
        this.type = type;
        this.id = id;
    }

}
