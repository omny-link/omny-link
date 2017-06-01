package link.omny.acctmgmt.model;

import javax.persistence.Column;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

@Data
public class FeatureConfig {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantConfig.class);

    private boolean account = false;
    private boolean accountCompanyDetails = true;
    private boolean accountDescription = true;
    private boolean accountView = false;
    private boolean activityAnalysis = false;
    private boolean activityTracker = false;
    private boolean addressAccount = false;
    private boolean addressContact = true;
    private boolean budget = false;
    private boolean catalog = true;
    private boolean companyBackground = false;
    private boolean contact = true;
    private boolean contactDescription = false;
    private boolean contactManagement = true;
    private boolean comms = true;
    private boolean enquiryType = true;
    private boolean feedback = false;
    private boolean financials = false;
    private boolean marketing = true;
    private boolean marketingDigital = marketing;
    private boolean multiNational = false;
    private boolean declaredSource = false;
    private boolean documents = false;
    private boolean documentsOnStockItem = false;
    private boolean documentsOnStockCategory = false;
    private boolean orders = false;
    private boolean orderDueDate = false;
    private boolean orderInvoiceRef = true;
    private boolean orderItems = false;
    private boolean merge = false;
    private boolean poweredBy = true;
    private boolean phone3 = false;
    @Column(name = "REFS")
    private boolean references = false;
    private boolean socialContact = true;
    private boolean socialAccount = true;
    private boolean stockCategory = false;
    private boolean stockCategoryImages = false;
    private boolean stockCategoryOffers = true;
    /** @deprecated Use stockCategoryOffers */
    private boolean offers = stockCategoryOffers;
    private boolean stockItemImages = false;
    private boolean stockItemOffers = false;
    private boolean stockLocation = false;
    private boolean stockPricing = true;
    private boolean stage = true;
    private boolean supportBar = true;
    private boolean workManagement = true;

    private String orderFieldsOnContact;

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
        case "accountView":
            setAccountView(b);
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
        case "catalog":
            setCatalog(b);
            break;
        case "comms":
            setComms(b);
            break;
        case "companyBackground":
            setCompanyBackground(b);
            break;
        case "contact":
            setContact(b);
            break;
        case "contactDescription":
            setContactDescription(b);
            break;
        case "contactManagement":
            setContactManagement(b);
        case "declaredSource":
            setDeclaredSource(b);
            break;
        case "documents":
            setDocuments(b);
            break;
        case "documentsOnStockCategory":
            setDocumentsOnStockCategory(b);
            break;
        case "documentsOnStockItem":
            setDocumentsOnStockItem(b);
            break;
        case "enquiryType":
            setEnquiryType(b);
            break;
        case "feedback":
            setFeedback(b);
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
            setStockCategoryOffers(b);
            break;
        case "orders":
            setOrders(b);
            break;
        case "orderDueDate":
            setOrderDueDate(b);
            break;
        case "orderInvoiceRef":
            setOrderInvoiceRef(b);
            break;
        case "orderItems":
            setOrderItems(b);
            break;
        case "phone3":
            setPhone3(b);
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
        case "stockCategoryImages":
            setStockCategoryImages(b);
            break;
        case "stockCategoryOffers":
            setStockCategoryOffers(b);
            break;
        case "stockItemImages":
            setStockItemImages(b);
            break;
        case "stockItemOffers":
            setStockItemOffers(b);
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
        case "workManagement":
            setWorkManagement(b);
            break;
        default:
            LOGGER.warn("Unsupported feature: " + name);
        }
    }

    public void set(String feature, int i) {
        switch (feature) {
        default:
            LOGGER.warn("Unsupported feature: " + feature);
        }
    }

    public void set(String feature, String value) {
        switch (feature) {
        case "orderFieldsOnContact":
            orderFieldsOnContact = value;
            break;
        default:
            LOGGER.warn("Unsupported feature: " + feature);
        }
    }
}
