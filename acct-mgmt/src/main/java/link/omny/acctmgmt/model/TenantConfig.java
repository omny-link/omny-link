package link.omny.acctmgmt.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single tenant's configuration.
 *
 * @author Tim Stephenson
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfig implements Serializable {
    private static final long serialVersionUID = -2041167810028725542L;
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantConfig.class);

    public static boolean resourceExists(String resourceUrl) {
        InputStream is = null;
        try {
            is = TenantConfig.class.getResourceAsStream(resourceUrl);
            return is != null;
        } catch (Exception e) {
            LOGGER.warn(String.format("Unable to find resource named %1$s",
                    resourceUrl));
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
        return false;
    }

    public static String readResource(String resource) {
        InputStream is = null;
        Reader source = null;
        Scanner scanner = null;
        try {
            is = TenantConfig.class.getResourceAsStream(resource);
            source = new InputStreamReader(is);
            scanner = new Scanner(source);
            return scanner.useDelimiter("\\A").next();
        } finally {
            try {
                scanner.close();
            } catch (Exception e) {
                ;
            }
        }
    }

    @Id
    protected String id;

    @JsonProperty
    protected String name;

    @Embedded
    private FeatureConfig features;

    @Embedded
    private ThemeConfig theme;

    @Embedded
    @Column(name = "service_level")
    private ServiceLevelConfig serviceLevel;

    @Transient
    private BotConfig bot;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Transient
    private List<ContactField> contactFields;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Transient
    private List<AccountField> accountFields;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Transient
    private List<OrderField> orderFields;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Transient
    private List<OrderItemField> orderItemFields;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Transient
    private List<OrderField> purchaseOrderFields;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Transient
    private List<FeedbackField> feedbackFields;

    @Transient
    private List<TenantSequence> sequences;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Transient
    private List<StockCategoryField> stockCategoryFields;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    // @JoinColumn(name = "tenantId")
    @Transient
    private List<StockItemField> stockItemFields;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    // TODO figure out how to persist
    @Transient
    private Map<String, String> strings;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER,
    // targetEntity = TenantToolbarEntry.class)
    @Transient
    private List<TenantToolbarEntry> toolbar;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER,
    // targetEntity = TenantProcess.class)
    @Transient
    private List<TenantProcess> processes;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER,
    // targetEntity = TenantPartial.class)
    @Transient
    private List<TenantPartial> partials;

    @Transient
    private List<TenantReport> reports;

    @Transient
    private List<TenantTemplate> templates;

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER,
    // targetEntity = TenantTypeaheadControl.class)
    @Transient
    private List<TenantTypeaheadControl> typeaheadControls;

    @Transient
    private List<TenantAction> accountActions;

    @Transient
    private List<TenantAction> contactActions;

    @Transient
    private List<TenantAction> orderActions;

    @Transient
    private List<TenantAction> purchaseOrderActions;

    @Transient
    private List<TenantAction> stockItemActions;

    @Transient
    private List<TenantAction> workActions;

    @JsonProperty
    @Transient
    public Long contacts;

    @JsonProperty
    @Transient
    public Long contactAlerts;

    @JsonProperty
    @Transient
    public Long definitions;

    @JsonProperty
    @Transient
    public Long activeInstances;

    @JsonProperty
    @Transient
    public Long historicInstances;

    @JsonProperty
    @Transient
    public Long jobs;

    @JsonProperty
    @Transient
    public Long tasks;

    @JsonProperty
    @Transient
    public Long users;

    @JsonProperty
    @Transient
    public Date lastLogin;

    public TenantConfig(String tenantId) {
        this();
        setId(tenantId);
    }

    public String getName() {
        if (name == null) {
            return id;
        } else {
            return name;
        }
    }

    public FeatureConfig getFeatures() {
        if (features == null) {
            features = new FeatureConfig();
        }
        return features;
    }

    public void setFeature(String feature, boolean b) {
        getFeatures().set(feature, b);
    }

    public void setFeature(String feature, int i) {
        getFeatures().set(feature, i);
    }

    public void setFeature(String feature, String value) {
        getFeatures().set(feature, value);
    }

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use setFeatures instead.
     */
    public void setShow(FeatureConfig features) {
        setFeatures(features);
    }

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use getFeatures instead.
     */
    public FeatureConfig getShow() {
        return getFeatures();
    }

    public List<TenantAction> getContactActions() {
        if (contactActions == null) {
            contactActions = new ArrayList<TenantAction>();
        }
        return contactActions;
    }

    public void addContactAction(TenantAction action) {
        getContactActions().add(action);
    }

    public List<TenantAction> getWorkActions() {
        if (workActions == null) {
            workActions = new ArrayList<TenantAction>();
        }
        return workActions;
    }

    public void addWorkAction(TenantAction action) {
        getWorkActions().add(action);
    }

    public List<TenantPartial> getPartials() {
        if (partials == null) {
            partials = new ArrayList<TenantPartial>();
        }
        return partials;
    }

    public void addPartial(TenantPartial tenantExtension) {
        getPartials().add(tenantExtension);
    }

    public List<TenantProcess> getProcesses() {
        if (processes == null) {
            processes = new ArrayList<TenantProcess>();
        }
        return processes;
    }

    public void addProcess(TenantProcess tenantExtension) {
        getProcesses().add(tenantExtension);
    }

    public List<TenantReport> getReports() {
        if (reports == null) {
            reports = new ArrayList<TenantReport>();
        }
        return reports;
    }

    public List<TenantToolbarEntry> getToolbar() {
        if (toolbar == null) {
            toolbar = new ArrayList<TenantToolbarEntry>();
        }
        return toolbar;
    }

    public void addToolbarEntry(TenantToolbarEntry tenantExtension) {
        getToolbar().add(tenantExtension);
    }

    public List<TenantTemplate> getTemplates() {
        if (templates == null) {
            templates = new ArrayList<TenantTemplate>();
        }
        return templates;
    }

    public void addTemplate(TenantTemplate template) {
        getTemplates().add(template);
    }

    public List<TenantTypeaheadControl> getTypeaheadControls() {
        if (typeaheadControls == null) {
            typeaheadControls = new ArrayList<TenantTypeaheadControl>();
        }
        return typeaheadControls;
    }

    public void addTypeaheadControl(TenantTypeaheadControl control) {
        getTypeaheadControls().add(control);
    }

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use setContactActions instead.
     */
    public void setCustomerActions(List<TenantAction> actions) {
        setContactActions(actions);
    }

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use getContactActions instead.
     */
    public List<TenantAction> getCustomerActions() {
        return getContactActions();
    }

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use setToolbar instead.
     */
    public void setOmnyBar(List<TenantToolbarEntry> entries) {
        setToolbar(entries);
    }

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use setToolbar instead.
     */
    public List<TenantToolbarEntry> getOmnyBar() {
        return getToolbar();
    }

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use setProcesses instead.
     */
    public void setStandardOperatingProcedures(List<TenantProcess> entries) {
        setProcesses(entries);
    }

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use getProcesses instead.
     */
    public List<TenantProcess> getStandardOperatingProcedures() {
        return getProcesses();
    }
}
