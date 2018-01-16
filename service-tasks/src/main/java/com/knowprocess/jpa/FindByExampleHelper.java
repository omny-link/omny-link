/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
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
package com.knowprocess.jpa;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;

public class FindByExampleHelper {

    private static EntityManager em;

    @Autowired
    public void setEntityManager(EntityManager em) {
        FindByExampleHelper.em = em;
    }

    public static List<?> findByExample(Object example,
            String sortFieldName, String sortOrder,
            List<String> fieldNames4OrderByClause) {
        Map<String, Object> params = new HashMap<String, Object>();
        String jpaQuery = buildQuery(example, sortFieldName, sortOrder, params,
                fieldNames4OrderByClause);
        Query q = entityManager().createQuery(jpaQuery, example.getClass());
        for (String field : fieldNames4OrderByClause) {
            Object obj = params.get(field);
            if (obj != null) {
                q.setParameter(field, params.get(field));
            }
        }
        return q.getResultList();
    }

    private static EntityManager entityManager() {
        if (em == null) {
            throw new RuntimeException("Entity manager has not been injected.");
        }
        return em;
    }

    /* package private to allow testing */static String buildQuery(
            Object example, String sortFieldName, String sortOrder,
            Map<String, Object> params, List<String> fieldNames4OrderByClause) {
        String jpaQuery = "SELECT o FROM " + example.getClass().getSimpleName()
                + " o";
        boolean whereAdded = false;

        for (String field : fieldNames4OrderByClause) {
            try {
                String accessorName = "get"
                        + field.substring(0, 1).toUpperCase()
                        + field.substring(1);
                Method method = example.getClass()
                        .getMethod(accessorName, null);
                Object obj = method.invoke(example);
                if (obj != null) {
                    params.put(field, obj);
                    if (whereAdded) {
                        jpaQuery = jpaQuery + " AND o." + field + " = :"
                                + field;
                    } else {
                        jpaQuery = jpaQuery + " WHERE o." + field + " = :"
                                + field;
                        whereAdded = true;
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Now add order by clause
        if (params.containsKey(sortFieldName)
                && fieldNames4OrderByClause.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY o." + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder)
                    || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        // System.out.println("Constructed query: " + jpaQuery);
        return jpaQuery;
    }

}
