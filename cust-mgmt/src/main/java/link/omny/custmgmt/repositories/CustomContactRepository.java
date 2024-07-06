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

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import link.omny.custmgmt.model.Contact;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;

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
        LOGGER.debug(String.format(
                "force load child entities, activities: %1$d, docs: %2$d, notes: %3$d",
                contact.getActivities().size(),
                contact.getDocuments().size(), contact.getNotes().size()));
        if (contact.getAccount() != null) {
                LOGGER.debug(String.format(
                        "force load acct child entities, "
                        + "custom fields %1$s, activities: %2$d, docs: %3$d, notes: %4$d",
                        contact.getAccount().getCustomFields().size(),
                        contact.getAccount().getActivities().size(),
                        contact.getAccount().getDocuments().size(),
                        contact.getAccount().getNotes().size()));
        }
        return contact;
    }

    protected Specification<Contact> stageIsUnspecified() {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .isNull(root.<String> get("stage"));
    }

    protected Specification<Contact> isNotDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .notEqual(root.<String> get("stage"), "deleted");
    }

    protected Specification<Contact> isTenant(String tenantId) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            return criteriaBuilder
                .equal(root.get("tenantId"), tenantId);
        };
    }

    public List<Contact> listForTenant(String tenantId, Pageable pageable) {
        long start = System.currentTimeMillis();
        LOGGER.info("List contacts for tenant {}", tenantId);

        List<Contact> list = contactRepo.findAll(
                isTenant(tenantId)
                        .and((stageIsUnspecified().or(isNotDeleted()))),
                pageable).getContent();

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
        q2.setHint("jakarta.persistence.fetchgraph", entityGraph);
        q2.setParameter("tenantId", tenantId);
        q2.setParameter("ids", ids);

        return (List<Contact>) q2.getResultList();
    }
}
