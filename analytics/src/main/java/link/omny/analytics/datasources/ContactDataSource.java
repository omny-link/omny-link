package link.omny.analytics.datasources;

import java.util.List;

import link.omny.analytics.api.ReportDataSource;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.repositories.ContactRepository;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
                    .getProperty(contacts.get(row - 1), field.getName());
        } catch (Exception e) {
            LOGGER.warn(String
                    .format("Unable to satisfy request for %1$s of contact with id %2$d",
                            field.getName(), contacts.get(row).getId()));
            return null;
        }
    }

    @Override
    public boolean next() throws JRException {
        return row++ < contacts.size();
    }

}
