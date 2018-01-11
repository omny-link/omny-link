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
package link.omny.custmgmt.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.custmgmt.model.Activity;

@RepositoryRestResource(path = "/activities")
public interface ActivityRepository extends CrudRepository<Activity, Long> {

//    @Query("SELECT a FROM Activity a JOIN a.contact c WHERE c.account.id = :accountId ORDER BY a.lastUpdated ASC")
//    List<Activity> findByAccountId(@Param("accountId") Long accountId);
//
//    @Query("SELECT a FROM Activity a WHERE a.contact.id = :contactId ORDER BY a.lastUpdated ASC")
//    List<Activity> findByContactId(@Param("contactId") Long contactId);

//    @Query("UPDATE #{#entityName} x set x.contact = :newContact where x.contact = :oldContact")
//    @Modifying(clearAutomatically = true)
//    public void updateContact(@Param("oldContact") Contact oldContact,
//            @Param("newContact") Contact newContact);

}
