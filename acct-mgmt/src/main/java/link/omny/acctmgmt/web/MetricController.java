/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package link.omny.acctmgmt.web;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

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

import link.omny.acctmgmt.model.Metric;
import link.omny.acctmgmt.repositories.MetricRepository;
import link.omny.acctmgmt.web.TenantController.TenantSummary;

@Controller
public class MetricController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MetricController.class);

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
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(TenantSummary.class);
            HashSet<Metric> metrics = new HashSet<Metric>();

            for (TenantSummary tenant : list) {
                metrics.clear();
                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                    Metric metric;
                    try {
                        Long value = (Long) pd.getReadMethod().invoke(tenant, new Object[0]);
                        if (value == null) {
                            throw new IllegalArgumentException(String.format(
                                    "%1$s is null, ignoring", pd.getName()));
                        }
                        metric = new Metric(tenant.getTenantId(), pd.getName(),
                               value, now);
                        metrics.add(metric);
                    } catch (IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | ClassCastException e) {
                        LOGGER.error("Unable to find metric {} for {}; cause: {}",
                                pd.getName(), tenant.getId(), e.getMessage());
                    }
                }
                metricRepo.save(metrics);
            }
        } catch (IntrospectionException e) {
            LOGGER.error(
                    "Cannot introspect TenantSummary, no metrics will be recorded",
                    e);
            throw new RuntimeException(e);
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
