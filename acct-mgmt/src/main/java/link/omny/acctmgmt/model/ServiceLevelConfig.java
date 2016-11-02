package link.omny.acctmgmt.model;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Data
public class ServiceLevelConfig {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ServiceLevelConfig.class);

    @Value("${omny.contact.inactiveStages:cold,complete,on hold,unqualified,waiting list" )
    private String[] inactiveStages;
    private Integer inactivityThreshold;
    private Integer initialResponseThreshold;

    public void set(String name, Object obj) {
        switch (name) {
        case "inactiveStages":
            setInactiveStages(((String) obj).split(","));
            break;
        case "inactivityThreshold":
            setInactivityThreshold((Integer) obj);
            break;
        case "initialResponseThreshold":
            setInitialResponseThreshold((Integer) obj);
            break;
        default:
            LOGGER.error("Unsupported service level: " + name);
        }
    }

}
