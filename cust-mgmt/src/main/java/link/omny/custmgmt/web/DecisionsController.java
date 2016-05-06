package link.omny.custmgmt.web;

import io.onedecision.engine.decisions.web.DecisionController;

import java.util.Map;
import java.util.Map.Entry;

import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomAccountField;
import link.omny.custmgmt.model.CustomContactField;
import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.custmgmt.repositories.NoteRepository;
import link.omny.custmgmt.web.fg.ValuationDecision;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Temporary stand-in for DMN based implementation
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/decisions")
public class DecisionsController extends DecisionController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(DecisionsController.class);

    @Autowired
    private ContactRepository repo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private NoteRepository noteRepo;

    @Autowired
    private ConversionService mvcConversionService;

    @Autowired
    private ValuationDecision valuationDecision;

    /**
     * @return contact updated with valuation.
     */
    @RequestMapping(value = "/valuation", method = RequestMethod.POST)
    public @ResponseBody ContactValuation runValuationDecision(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Contact contact) {

        LOGGER.info(String
                .format("Running valuation for tenant %1$s", tenantId));
        // May be redundant but better safe than sorry
        contact.setTenantId(tenantId);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(
                    "Received valuation request containing: %1$s",
                    contact.getCustomFields()));
        }

        Map<String, Double> results = valuationDecision.calc(
                mvcConversionService.convert(
                        contact.getCustomFieldValue("operatingProfit"), Double.class),
                mvcConversionService.convert(
                        contact.getCustomFieldValue("depreciationAmortisation"),
                        Double.class), 
                mvcConversionService.convert(
                        contact.getCustomFieldValue("adjustments"), Double.class),
                mvcConversionService.convert(contact.getCustomFieldValue("ebitda"),
                        Double.class), 
                mvcConversionService.convert(
                        contact.getCustomFieldValue("surplus"), Double.class),
                mvcConversionService.convert(contact.getCustomFieldValue("borrowing"),
                        Double.class));

        for (Entry<String, Double> entry : results.entrySet()) {
            contact.addCustomField(new CustomContactField(entry.getKey(), entry
                    .getValue()));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(
                    "Returning contact containing valuation: %1$s",
                    contact.getCustomFields()));
        }
        return wrap(contact);
    }

    private ContactValuation wrap(Contact contact) {
        ContactValuation resource = new ContactValuation();
        BeanUtils.copyProperties(contact, resource);
        for (CustomContactField field: contact.getCustomFields()) {
            resource.getAccount().addCustomField(
                    new CustomAccountField(field.getName(), field.getValue()));
        }
        Link detail = linkTo(ContactRepository.class, contact.getId())
                .withSelfRel();
        resource.add(detail);
        resource.getAccount().setTenantId(contact.getTenantId());
        resource.getAccount().setName((String) contact.getCustomFieldValue("accountName"));
        resource.add(linkTo(AccountRepository.class, contact.getId()).withRel(
                "account"));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Wrapped contact: %1$s", resource));
        }
        return resource;
    }
    
    private Link linkTo(Class<? extends CrudRepository<?, ?>> clazz, Long id) {
        return new Link(clazz.getAnnotation(RepositoryRestResource.class)
                .path() + "/" + id);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ContactValuation extends ResourceSupport {
        private String firstName;
        private String lastName;
        private String email;
        private String owner;
        private String stage;
        private String accountType;
        private String enquiryType;
        private String tenantId;
        private boolean doNotCall;
        private boolean doNotEmail;
        private String source;
        private String medium;
        private String campaign;
        private String keyword;

        private Account account;

        // @JsonDeserialize(using = JsonCustomContactFieldDeserializer.class)
        // @JsonSerialize(using = JsonCustomFieldSerializer.class)
        // private List<CustomAccountField> customFields;
        public Account getAccount() {
            if (account == null) {
                account = new Account();
            }
            return account;
        }
      }
}
