package link.omny.supportservices.exceptions;

/**
 * Business exception suitable for catching as BPMN error.
 *
 * @author Tim Stephenson
 */
public class BusinessEntityNotFoundException extends RuntimeException {

    private String entity;
    private String id;

    public BusinessEntityNotFoundException(String entity, String id) {
        super();
        this.entity = entity;
        this.id = id;
    }

    private static final long serialVersionUID = 7932887356223861356L;

    public String getEntity() {
        return entity;
    }

    public String getId() {
        return id;
    }

}
