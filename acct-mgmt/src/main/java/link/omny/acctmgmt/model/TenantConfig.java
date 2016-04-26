package link.omny.acctmgmt.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The model class to encapsulate a single tenant's configuration of Omny.
 *
 * @author Tim Stephenson
 */
@Data
@Entity
@Table(name = "OL_TENANT")
@NoArgsConstructor
@AllArgsConstructor
// TODO This is intended to replace the current JSON file, in particular to
// allow user-management of drop down lists and also but also to remove the need
// for re-deployment to update those JSON files.
public class TenantConfig implements Serializable {
    private static final long serialVersionUID = -2041167810028725542L;

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

    @Transient
    private BotConfig bot;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    // @JoinColumn(name = "ENTITY_ID")
    private List<ContactField> contactFields;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    // @JoinColumn(name = "ENTITY_ID")
    private List<AccountField> accountFields;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = TenantToolbarEntry.class)
    private List<TenantToolbarEntry> toolbar;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = TenantProcess.class)
    private List<TenantProcess> processes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = TenantPartial.class)
    private List<TenantPartial> partials;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = TenantTypeaheadControl.class)
    private List<TenantTypeaheadControl> typeaheadControls;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = TenantAction.class)
    private List<TenantAction> contactActions;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = TenantAction.class)
    private List<TenantAction> workActions;

    public TenantConfig(String tenantId) {
        this();
        setId(tenantId);
    }

    public void setId(String id) {
        this.id = id;
        for (TenantAction action : getContactActions()) {
            action.setTenant(this);
        }
        for (TenantPartial partial : getPartials()) {
            partial.setTenant(this);
        }
        for (TenantProcess process : getProcesses()) {
            process.setTenant(this);
        }
        for (TenantToolbarEntry toolbarEntry : getToolbar()) {
            toolbarEntry.setTenant(this);
        }
        for (TenantTypeaheadControl control : getTypeaheadControls()) {
            control.setTenant(this);
        }
        for (TenantAction action : getWorkActions()) {
            action.setTenant(this);
        }
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

    public void setFeature(String string, boolean b) {
        getFeatures().set("account", true);
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
        action.setTenant(this);
        getContactActions().add(action);
    }

    public List<TenantAction> getWorkActions() {
        if (workActions == null) {
            workActions = new ArrayList<TenantAction>();
        }
        return workActions;
    }

    public void addWorkAction(TenantAction action) {
        action.setTenant(this);
        getWorkActions().add(action);
    }

    public List<TenantPartial> getPartials() {
        if (partials == null) {
            partials = new ArrayList<TenantPartial>();
        }
        return partials;
    }

    public void addPartial(TenantPartial tenantExtension) {
        tenantExtension.setTenant(this);
        getPartials().add(tenantExtension);
    }

    public List<TenantProcess> getProcesses() {
        if (processes == null) {
            processes = new ArrayList<TenantProcess>();
        }
        return processes;
    }

    public void addProcess(TenantProcess tenantExtension) {
        tenantExtension.setTenant(this);
        getProcesses().add(tenantExtension);
    }

    public List<TenantToolbarEntry> getToolbar() {
        if (toolbar == null) {
            toolbar = new ArrayList<TenantToolbarEntry>();
        }
        return toolbar;
    }

    public void addToolbarEntry(TenantToolbarEntry tenantExtension) {
        tenantExtension.setTenant(this);
        getToolbar().add(tenantExtension);
    }

    public List<TenantTypeaheadControl> getTypeaheadControls() {
        if (typeaheadControls == null) {
            typeaheadControls = new ArrayList<TenantTypeaheadControl>();
        }
        return typeaheadControls;
    }

    public void addTypeaheadControl(TenantTypeaheadControl control) {
        control.setTenant(this);
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
