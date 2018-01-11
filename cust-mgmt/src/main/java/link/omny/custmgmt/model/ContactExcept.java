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
package link.omny.custmgmt.model;

import java.util.Date;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "excerpt", types = { Account.class, Contact.class })
public interface ContactExcept {

    Long getId();

    String getFirstName();

    String getLastName();

    String getTitle();

    String getEmail();

    String getPhone1();

    String getStage();

    String getOwner();

    boolean getDoNotCall();

    boolean getDoNotEmail();

    String getTags();

    Date getFirstContact();

    Date getLastUpdated();

    String getTenantId();

    Account getAccount();
}
