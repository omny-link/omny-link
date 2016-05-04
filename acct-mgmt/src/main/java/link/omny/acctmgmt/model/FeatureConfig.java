package link.omny.acctmgmt.model;

import lombok.Data;

@Data
public class FeatureConfig {

    private boolean account = false;
    private boolean activityAnalysis = false;
    private boolean budget = false;
    private boolean companyBackground = false;
    private boolean marketing = false;
    private boolean merge = false;
    private boolean poweredBy = true;

    public void set(String name, boolean b) {
        switch (name) {
        case "account":
            setAccount(b);
            break;
        case "activityAnalysis":
            setActivityAnalysis(b);
            break;
        case "budget":
            setBudget(b);
            break;
        case "companyBackground":
            setCompanyBackground(b);
            break;
        case "marketing":
            setMarketing(b);
            break;
        case "merge":
            setMerge(b);
            break;
        case "poweredBy":
            setPoweredBy(b);
            break;
        default:
            System.err.println("Unsupported feature: " + name);
        }
    }

}
