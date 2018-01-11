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
import java.util.List;

import link.omny.custmgmt.json.JsonCustomContactFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;

import org.springframework.data.rest.core.config.Projection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Projection(name = "base", types = { Account.class, Activity.class,
        Contact.class, CustomAccountField.class, CustomContactField.class,
        Document.class, Note.class })
public interface ContactBaseProjection {

    Long getId();

    String getFirstName();

    String getLastName();

    String getFullName();

    String getTitle();

    String getJobTitle();

    String getEmail();

    boolean getEmailConfirmed();

    String getEmailConfirmationCode();

    String getUuid();

    String getPhone1();

    String getPhone2();

    String getAddress1();

    String getAddress2();

    String getTown();

    String getCountyOrCity();

    String getPostCode();

    String getCountry();

    String getEnquiryType();

    String getAccountType();

    String getStage();

    String getStageReason();

    Date getStageDate();

    String getOwner();

    String getSource();

    String getMedium();

    String getCampaign();

    String getKeyword();

    boolean getDoNotCall();

    boolean getDoNotEmail();

    String getTags();

    String getTwitter();

    String getFacebook();

    String getLinkedIn();

    Date getFirstContact();

    Date getLastUpdated();

    String getTenantId();

    Account getAccount();

    Long getAccountId();

    @JsonDeserialize(using = JsonCustomContactFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    List<CustomContactField> getCustomFields();

    long getTimeSinceBusinessPlanDownload();

    long getTimeSinceLogin();

    long getTimeSinceFirstLogin();

    long getTimeSinceRegistered();

    long getTimeSinceEmail();

    int getEmailsSent();

    long getTimeSinceValuation();

}
