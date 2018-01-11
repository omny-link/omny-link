/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
package link.omny.supportservices.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import link.omny.supportservices.model.NumberSequence;
import link.omny.supportservices.repositories.NumberSequenceRepository;
import link.omny.supportservices.views.NumberSequenceViews;

@Controller
@RequestMapping(value = "/{tenantId}/sequences")
public class NumberSequenceController {

    @Autowired
    private NumberSequenceRepository nfRepo;

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    @JsonView(NumberSequenceViews.Summary.class)
    @Transactional(value = TxType.REQUIRED)
    public @ResponseBody NumberSequence getNext(
            @PathVariable("name") @NotNull String name,
            @PathVariable("tenantId") @NotNull String tenantId) {
        NumberSequence next;
        try {
            List<NumberSequence> last = nfRepo.findByEntityNameForTenant(name,
                    tenantId);
            if (last == null || last.size() == 0) {
                throw new NoSuchElementException(String
                        .format("Cannot find sequence %1$s for %2$s", name,
                                tenantId));
            } else if (last.size() > 1) {
                // just in case someone's been messing with the database
                Collections.sort(last, new Comparator<NumberSequence>() {
                    @Override
                    public int compare(NumberSequence o1, NumberSequence o2) {
                        return o1.getLastUsed().compareTo(o2.getLastUsed());
                    }
                });
                next = last.get(0);
                for (int i = 1; i < last.size(); i++) {
                    nfRepo.delete(last.get(i));
                }
            } else {
                next = last.get(0);
            }
            next.increment();
            next = nfRepo.save(next);
        } catch (NoSuchElementException e) {
            next = initFountain(name, tenantId);
        }
        return addLinks(tenantId, next);
    }

    protected NumberSequence initFountain(String entityName, String tenantId) {
        NumberSequence numberFountain = new NumberSequence(entityName,
                tenantId);
        return nfRepo.save(numberFountain);
    }

    private NumberSequence addLinks(String tenantId, NumberSequence seq) {
        List<Link> links = new ArrayList<Link>();
        links.add(new Link(String.format("/%1$s/sequences/%2$s", tenantId,
                seq.getId())));
        seq.setLinks(links);
        return seq;
    }
}
