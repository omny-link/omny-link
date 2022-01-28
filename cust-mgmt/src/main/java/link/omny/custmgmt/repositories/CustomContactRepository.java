/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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
package link.omny.custmgmt.repositories;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import link.omny.custmgmt.model.Contact;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import link.omny.supportservices.model.Activity;
import link.omny.supportservices.model.Document;
import link.omny.supportservices.model.Note;

@Component
public class CustomContactRepository {

    public static final Logger LOGGER = LoggerFactory
            .getLogger(CustomContactRepository.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ContactRepository contactRepo;

    @Transactional
    public Contact findById(final String tenantId, final Long contactId) {
        Contact contact = contactRepo.findById(contactId)
                .orElseThrow(() -> new BusinessEntityNotFoundException(
                        Contact.class, contactId));
        Set<Activity> activities = contact.getActivities();
        Set<Document> documents = contact.getDocuments();
        Set<Note> notes = contact.getNotes();
        LOGGER.debug(String.format(
                "force load of child entities separately,"
                        + " activities: %1$d, docs: %2$d, notes: %3$d",
                activities.size(), documents.size(), notes.size()));
        return contact;
    }

    public List<Contact> listForTenant(String tenantId, Pageable pageable) {
        long start = System.currentTimeMillis();
        LOGGER.info("List contacts for tenant {}", tenantId);

        List<Contact> list = findAllByTenantAndIds(tenantId,
                findPageByTenant(tenantId, pageable));
        LOGGER.info("Found {} {} contacts in {}ms", list.size(), tenantId,
                (System.currentTimeMillis() - start));
        return list;
    }

    private List<Long> findPageByTenant(String tenantId, Pageable pageable) {
        TypedQuery<Long> q = entityManager.createQuery(
                ContactRepository.FIND_ID_FOR_TENANT,
                Long.class);
        q.setParameter("tenantId", tenantId);
        q.setFirstResult((int) pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());

        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<Contact> findAllByTenantAndIds(String tenantId,
            List<Long> ids) {
        EntityGraph<? super Contact> entityGraph = (EntityGraph<? super Contact>) entityManager
                .getEntityGraph("contactWithAccount");
        TypedQuery<?> q2 = entityManager.createQuery(
                ContactRepository.FIND_ALL_BY_TENANT_AND_IDS, Contact.class);
        q2.setHint("javax.persistence.fetchgraph", entityGraph);
        q2.setParameter("tenantId", tenantId);
        q2.setParameter("ids", ids);

        return (List<Contact>) q2.getResultList();
    }
}
