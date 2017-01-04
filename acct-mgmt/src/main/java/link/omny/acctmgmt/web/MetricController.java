package link.omny.acctmgmt.web;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import link.omny.acctmgmt.model.Metric;
import link.omny.acctmgmt.model.SystemConfig;
import link.omny.acctmgmt.repositories.MetricRepository;
import link.omny.acctmgmt.web.TenantController.TenantSummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class MetricController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MetricController.class);

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    protected TenantController tenantController;

    @Autowired
    protected MetricRepository metricRepo;

    /**
     * Take a new recording of metrics for each tenant.
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/admin/metrics", method = RequestMethod.POST)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public @ResponseBody ResponseEntity<?> record() {
        Date now = new Date();

        List<TenantSummary> list = tenantController
                .showAllTenants();
        for (TenantSummary tenant : list) {
            Metric defnMetric = new Metric(tenant.getTenantId(), "definitions",
                    tenant.getDefinitions(), now);
            metricRepo.save(defnMetric);
            Metric historicInstancesMetric = new Metric(tenant.getTenantId(),
                    "historicInstances", tenant.getHistoricInstances(), now);
            metricRepo.save(historicInstancesMetric);
            Metric instanceMetric = new Metric(tenant.getTenantId(),
                    "instances", tenant.getActiveInstances(), now);
            metricRepo.save(instanceMetric);
            Metric jobMetric = new Metric(tenant.getTenantId(), "jobs",
                    tenant.getJobs(), now);
            metricRepo.save(jobMetric);
            Metric userMetric = new Metric(tenant.getTenantId(), "users",
                    tenant.getUsers(), now);
            metricRepo.save(userMetric);
        }
        UriComponentsBuilder builder = MvcUriComponentsBuilder
                .fromController(getClass());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/admin/metrics").build().toUri());

        return new ResponseEntity(headers, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/admin/metrics", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody Iterable<Metric> showAllJson(
            @RequestParam(value = "window", required = false) Integer window) {
        LOGGER.info(String.format("showAllJson"));

        if (window == null) {
            window = 90;
        }
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DATE, -window);
        List<Metric> list = metricRepo.findAllSinceDate(cal.getTime());
        LOGGER.debug(String.format("Found %1$s metrics", list.size()));

        return list;
    }

    @RequestMapping(value = "/{tenantId}/metrics", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody Iterable<Metric> showAllForTenantJson(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "window", required = false) Integer window) {
        LOGGER.info(String.format("showAllForTenantJson"));

        if (window == null) {
            window = 90;
        }
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DATE, -window);
        List<Metric> list = metricRepo.findAllSinceDateForTenant(cal.getTime(),
                tenantId);
        LOGGER.debug(String.format("Found %1$s metrics", list.size()));

        return list;
    }

    @RequestMapping(value = "/{tenantId}/metrics/{name}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody Iterable<Metric> showNamedJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("name") String name,
            @RequestParam(value = "window", required = false) Integer window) {
        LOGGER.info(String.format("showNamedJson"));

        if (window == null) {
            window = 90;
        }
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DATE, -window);
        List<Metric> list = metricRepo.findByNameSinceDateForTenant(name,
                cal.getTime(), tenantId);
        LOGGER.debug(String.format("Found %1$s metrics", list.size()));

        return list;
    }
}
