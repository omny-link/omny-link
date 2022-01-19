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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import link.omny.custmgmt.model.Account;

@Component
public class CustomAccountRepository {

    public static final Logger LOGGER = LoggerFactory
            .getLogger(CustomAccountRepository.class);

    @Autowired
    public AccountRepository accountRepo;

    protected Specification<Account> stageIsUnspecified() {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .isNull(root.<String> get("stage"));
    }

    protected Specification<Account> isNotDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .notEqual(root.<String> get("stage"), "deleted");
    }

    protected Specification<Account> isTenant(String tenantId) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            return criteriaBuilder
                .equal(root.get("tenantId"), tenantId);
        };
    }

    public List<Account> listForTenant(String tenantId) {
        return listForTenant(tenantId, 0, 0);
    }

    public List<Account> listForTenant(String tenantId, Integer page,
            Integer limit) {
        long start = System.currentTimeMillis();
        LOGGER.info("List accounts for tenant {}", tenantId);

        List<Account> list;
        if (limit == null) {
            list = accountRepo.findAll(isTenant(tenantId)
                    .and((stageIsUnspecified().or(isNotDeleted()))));
        } else {
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = accountRepo.findAll(
                    isTenant(tenantId)
                            .and((stageIsUnspecified().or(isNotDeleted()))),
                    pageable).getContent();
        }

        LOGGER.info("Found {} {} accounts in {}ms", list.size(), tenantId,
                (System.currentTimeMillis()-start));
        return list;
    }

}
