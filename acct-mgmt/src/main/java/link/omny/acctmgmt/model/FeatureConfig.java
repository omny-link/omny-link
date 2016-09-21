package link.omny.acctmgmt.model;

import javax.persistence.Column;

import lombok.Data;

@Data
public class FeatureConfig {

    private boolean account = false;
    private boolean activityAnalysis = false;
    private boolean activityTracker = false;
    private boolean budget = false;
    private boolean companyBackground = false;
    private boolean financials = false;
    private boolean marketing = false;
    private boolean declaredSource = false;
    private boolean documents = false;
    private boolean orders = false;
    private boolean merge = false;
    private boolean poweredBy = true;
    @Column(name = "REFS")
    private boolean references = false;
    private boolean stockCategory = false;
    private boolean supportBar = true;

    public void set(String name, boolean b) {
        switch (name) {
        case "account":
            setAccount(b);
            break;
        case "activityAnalysis":
            setActivityAnalysis(b);
            break;
        case "activityTracker":
            setActivityTracker(b);
            break;
        case "budget":
            setBudget(b);
            break;
        case "companyBackground":
            setCompanyBackground(b);
            break;
        case "declaredSource":
            setDeclaredSource(b);
            break;
        case "documents":
            setDeclaredSource(b);
            break;
        case "financials":
            setDeclaredSource(b);
            break;
        case "marketing":
            setMarketing(b);
            break;
        case "merge":
            setMerge(b);
            break;
        case "orders":
            setOrders(b);
            break;
        case "poweredBy":
            setPoweredBy(b);
            break;
        case "references":
            setReferences(b);
            break;
        case "stockCategory":
            setStockCategory(b);
            break;
        case "supportBar":
            setSupportBar(b);
            break;
        default:
            System.err.println("Unsupported feature: " + name);
        }
    }

}
