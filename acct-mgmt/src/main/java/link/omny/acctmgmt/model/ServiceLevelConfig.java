package link.omny.acctmgmt.model;

import lombok.Data;

@Data
public class ServiceLevelConfig {

    private String[] inactiveStages = { "cold", "complete" };
    private int inactivityThreshold = 14;
    private int initialResponseThreshold = 7;

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
            System.err.println("Unsupported service level: " + name);
        }
    }

}
