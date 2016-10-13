package link.omny.acctmgmt.model;

import javax.persistence.Column;

import lombok.Data;

@Data
public class FeatureConfig {

    private boolean account = false;
    private boolean accountCompanyDetails = true;
    private boolean accountDescription = true;
    private boolean activityAnalysis = false;
    private boolean activityTracker = false;
    private boolean addressAccount = false;
    private boolean addressContact = true;
    private boolean budget = false;
    private boolean companyBackground = false;
    private boolean contactDescription = false;
    private boolean enquiryType = true;
    private boolean financials = false;
    private boolean marketing = true;
    private boolean marketingDigital = marketing;
    private boolean multiNational = false;
    private boolean declaredSource = false;
    private boolean documents = false;
    private boolean offers = true;
    private boolean orders = false;
    private boolean merge = false;
    private boolean poweredBy = true;
    @Column(name = "REFS")
    private boolean references = false;
    private boolean socialContact = true;
    private boolean socialAccount = true;
    private boolean stockCategory = false;
    private boolean stockLocation = false;
    private boolean stockPricing = true;
    private boolean stage = true;
    private boolean supportBar = true;

    public void set(String name, boolean b) {
        switch (name) {
        case "account":
            setAccount(b);
            break;
        case "accountCompanyDetails":
            setAccountCompanyDetails(b);
            break;
        case "accountDescription":
            setAccountDescription(b);
            break;
        case "activityAnalysis":
            setActivityAnalysis(b);
            break;
        case "activityTracker":
            setActivityTracker(b);
            break;
        case "addressAccount":
            setAddressAccount(b);
            break;
        case "addressContact":
            setAddressContact(b);
            break;
        case "budget":
            setBudget(b);
            break;
        case "companyBackground":
            setCompanyBackground(b);
            break;
        case "contactDescription":
            setContactDescription(b);
            break;
        case "declaredSource":
            setDeclaredSource(b);
            break;
        case "documents":
            setDocuments(b);
            break;
        case "enquiryType":
            setEnquiryType(b);
            break;
        case "financials":
            setFinancials(b);
            break;
        case "marketing":
            setMarketing(b);
            break;
        case "marketingDigital":
            setMarketingDigital(b);
            break;
        case "merge":
            setMerge(b);
            break;
        case "multiNational":
            setMultiNational(b);
            break;
        case "offers":
            setOffers(b);
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
        case "socialAccount":
            setSocialAccount(b);
            break;
        case "socialContact":
            setSocialContact(b);
            break;
        case "stage":
            setStage(b);
            break;
        case "stockCategory":
            setStockCategory(b);
            break;
        case "stockLocation":
            setStockLocation(b);
            break;
        case "stockPricing":
            setStockPricing(b);
            break;
        case "supportBar":
            setSupportBar(b);
            break;
        default:
            System.err.println("Unsupported feature: " + name);
        }
    }

}
