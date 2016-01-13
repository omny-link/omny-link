package link.omny.acctmgmt.web;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import link.omny.acctmgmt.model.Metric;
import link.omny.acctmgmt.repositories.MetricRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("{tenantId}/metrics")
public class MetricController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MetricController.class);

    @Autowired
    protected MetricRepository metricRepo;

    @RequestMapping(value = "/", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody Iterable<Metric> showAllJson(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "window", required = false) Integer window) {
        LOGGER.info(String.format("showAllJson"));

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

    @RequestMapping(value = "/{name}", method = RequestMethod.GET, headers = "Accept=application/json")
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
