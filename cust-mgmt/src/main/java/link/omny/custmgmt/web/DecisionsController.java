package link.omny.custmgmt.web;

import java.util.Map;
import java.util.Map.Entry;

import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomContactField;
import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.custmgmt.repositories.NoteRepository;
import link.omny.custmgmt.web.fg.FollowUpDecision;
import link.omny.custmgmt.web.fg.ValuationDecision;
import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.knowprocess.mail.MailData;

/**
 * Temporary stand-in for DMN based implementation
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/decisions")
public class DecisionsController {

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

    @Autowired
    private FollowUpDecision followUpDecision;

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
                        contact.getField("operatingProfit"), Double.class),
                mvcConversionService.convert(
                        contact.getField("depreciationAmortisation"),
                        Double.class), 
                mvcConversionService.convert(
                        contact.getField("adjustments"), Double.class),
                mvcConversionService.convert(contact.getField("ebitda"),
                        Double.class), 
                mvcConversionService.convert(
                        contact.getField("surplus"), Double.class),
                mvcConversionService.convert(contact.getField("borrowing"),
                        Double.class));

        for (Entry<String, Double> entry : results.entrySet()) {
            contact.setField(entry.getKey(), entry.getValue());
        }

        return wrap(contact);
    }

    /**
     * @return details of mail to send.
     */
    @RequestMapping(value = "/email", method = RequestMethod.POST, produces = { "application/json" })
    public @ResponseBody String runEmailDecision(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Contact contact) {

        LOGGER.info(String.format("Running email follow-up for tenant %1$s",
                tenantId));
        // May be redundant but better safe than sorry
        contact.setTenantId(tenantId);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Received contact: %1$s",
                    contact.getFullName()));
        }

        MailData mailData = followUpDecision.execute(contact);
        
        LOGGER.debug(String.format("Mail data: %1$s", mailData));
        return mailData == null ? null : mailData.toJson();
    }

    private ContactValuation wrap(Contact contact) {
        ContactValuation resource = new ContactValuation();
        resource.setFirstName(contact.getFirstName());
        resource.setLastName(contact.getLastName());
        resource.setEmail(contact.getEmail());
        resource.setOwner(contact.getOwner());
        resource.setStage(contact.getStage());
        resource.setTenantId(contact.getTenantId());
        for (CustomContactField field: contact.getCustomFields()) {
            resource.getAccount().setField(field.getName(), field.getValue());
        }
        Link detail = linkTo(ContactRepository.class, contact.getId())
                .withSelfRel();
        resource.add(detail);
        resource.getAccount().setTenantId(contact.getTenantId());
        resource.getAccount().setName((String) contact.getField("accountName"));
        resource.add(linkTo(AccountRepository.class, contact.getId()).withRel(
                "account"));
        return resource;
    }
    
    
    private Link linkTo(Class<? extends CrudRepository<?, ?>> clazz, Long id) {
        return new Link(clazz.getAnnotation(RepositoryRestResource.class)
                .path() + "/" + id);
    }

    @Data
    public static class ContactValuation extends ResourceSupport {
        private String firstName;
        private String lastName;
        private String email;
        private String owner;
        private String stage;
        private String tenantId;
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
