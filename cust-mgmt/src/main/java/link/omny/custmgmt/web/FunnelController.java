package link.omny.custmgmt.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import link.omny.custmgmt.repositories.ContactRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * REST web service for fetching contacts aggregated in a way convenient for
 * reporting.
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/analytics/contacts")
public class ContactAnalyticsController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ContactAnalyticsController.class);

    @Autowired
    private ContactRepository contactRepo;


    /**
     * Return funnel report data.
     * 
     * @return contacts for that tenant.
     */
    @RequestMapping(value = "/funnel", method = RequestMethod.GET)
    public @ResponseBody FunnelReport listForTenant(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("List contacts for tenant %1$s", tenantId));

        FunnelReport rpt = new FunnelReport();
        List<Object[]> list = contactRepo
                .findAllForTenantGroupByStage(tenantId);
        LOGGER.debug(String.format("Found %1$s stages", list.size()));

        for (Object[] objects : list) {
            rpt.addStage((String) objects[0], (Number) objects[1]);
        }

        return rpt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class FunnelReport extends ResourceSupport {
        @JsonProperty
        private Map<String, Number> stages;

        public Map<String, Number> getStages() {
            if (stages == null) {
                stages = new HashMap<String, Number>();
            }
            return stages;
        }

        public void addStage(String stage, Number count) {
            getStages().put(
                    (stage == null || stage.length() == 0)
                    ? "N/A" : stage, count == null ? 0 : count);
        }
      }
}
