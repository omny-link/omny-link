package link.omny.analytics.datasources;

import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import link.omny.analytics.api.ReportDataSource;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomContactField;
import link.omny.custmgmt.repositories.ContactRepository;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

@Component
public class ContactDataSource implements ReportDataSource {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ContactDataSource.class);

    @Autowired
    private ContactRepository contactRepo;

    private int row = 0;

    private List<Contact> contacts;

    public void init(String tenantId) {
        contacts = contactRepo.findAllForTenant(tenantId);
        row = 0;
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        try {
            return BeanUtils
                    .getNestedProperty(contacts.get(row - 1), field.getName());
        } catch (NoSuchMethodException e) {
            LOGGER.info(String
                    .format("Unable to satisfy request for %1$s of contact with id %2$d, searching for custom field",
                            field.getName(), contacts.get(row-1).getId()));
            try {
                for (CustomContactField customField : contacts.get(row-1).getCustomFields()) {
                    if (customField.getName().equals(field.getName())) {
                        return customField.getValue();
                    }
                }
                return handleException(field);
            } catch (Exception e1) {
                return handleException(field);
            }
        } catch (Exception e) {
            return handleException(field);
        }
    }

    private Object handleException(JRField field) {
        LOGGER.warn(String
                .format("Unable to satisfy request for %1$s of contact with id %2$d",
                        field.getName(), contacts.get(row-1).getId()));
        return null;
    }

    @Override
    public boolean next() throws JRException {
        return row++ < contacts.size();
    }

}
