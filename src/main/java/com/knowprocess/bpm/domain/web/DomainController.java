package com.knowprocess.bpm.domain.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.domain.model.CustomEntityField;
import com.knowprocess.bpm.domain.model.DomainEntity;
import com.knowprocess.bpm.domain.model.DomainModel;
import com.knowprocess.bpm.domain.model.EntityField;

@Controller
@RequestMapping(value = "/{tenantId}/domain")
public class DomainController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody DomainModel getModelForTenant(
            @PathVariable("tenantId") String tenantId) {
        switch (tenantId) {
        case "firmgains":
            return getFirmGainsDomain();
        default:
            return getDefaultDomain();
        }
    }

    private DomainModel getDefaultDomain() {
        DomainModel model = new DomainModel();
        model.setName("Omny Link customer model");
        model.setDescription("A general purpose and extensible customer model for the web");

        List<DomainEntity> entities = new ArrayList<DomainEntity>();

        // Contact
        DomainEntity entity = new DomainEntity();
        entity.setName("Contact");
        entity.setDescription("A Contact is associated with up to one Account, zero to many Notes and zero to many Documents. An Account typically has one contact though may have more.");
        entity.setImageUrl("images/domain/contact-context.png");
        List<EntityField> fields = new ArrayList<EntityField>();
        fields.add(new EntityField("firstName", "First Name",
                "Your first or given name", true, "text"));
        fields.add(new EntityField("lastName", "Last Name",
                "Your last or family name", true, "text"));
        fields.add(new EntityField("title", "Title", "Your title / salutation",
                false, "text"));
        fields.add(new EntityField("email", "Email Address",
                "Your business email address", true, "text"));
        fields.add(new EntityField("phone1", "Preferred Phone Number",
                "Your preferred telephone number", false, "tel",
                "\\+?[0-9, ]{0,13}"));
        fields.add(new EntityField("phone2", "Other Phone Number",
                "A backup telephone number", false, "tel", "\\+?[0-9, ]{0,13}"));
        fields.add(new EntityField("address1", "Address",
                "House or apartment name or number", false, "text"));
        fields.add(new EntityField("address2", "", "Stree", false, "text"));
        fields.add(new EntityField("countyOrCity", "City or County", "", false,
                "text"));
        fields.add(new EntityField("postCode", "Post Code",
                "Postal code, example N1 9DH", false, "text", ""));
        fields.add(new EntityField("stage", "Stage",
                "The point in the sales funnel of this lead", false, "text"));
        fields.add(new EntityField(
                "enquiryType",
                "Enquiry Type",
                "The nature of the enquiry, typically specific to the tenant's business",
                false, "text"));
        fields.add(new EntityField("accountType", "Account Type",
                "Customer, Partner etc.", false, "text"));
        fields.add(new EntityField("owner", "Owner",
                "The sales person for this account", false, "text"));
        fields.add(new EntityField("doNotCall", "Do Not Call",
                "Is it ok to call this lead?", false, "boolean"));
        fields.add(new EntityField("doNotEmail", "Do Not Email",
                "Is it ok to email this lead?", false, "boolean"));
        fields.add(new EntityField(
                "source",
                "Source",
                "Where this lead came from, typically auto-populated by pay-per-click system",
                false, "text"));
        fields.add(new EntityField(
                "medium",
                "Medium",
                "Medium this lead came via, typically auto-populated by pay-per-click system",
                false, "text"));
        fields.add(new EntityField("campaign", "Campaign",
                "Additional information from the pay-per-click system", false,
                "text"));
        fields.add(new EntityField("keyword", "Keyword",
                "Additional information from the pay-per-click system", false,
                "text"));
        fields.add(new EntityField("timeSinceEmail", "Time since email",
                "Time since our last email to contact (milliseconds)", false,
                "number"));
        fields.add(new EntityField("timeSinceLogin", "Time since login",
                "Time since last login (milliseconds)", false, "number"));
        fields.add(new EntityField("timeSinceRegistered",
                "Time since registered",
                "Time since last registered (milliseconds)", false, "number"));
        fields.add(new EntityField("tenantId", "Tenant",
                "Name of the the Omny account", true, "text"));
        fields.add(new EntityField("firstContact", "First Contact",
                "Date of first contact with this business", true, "date"));
        fields.add(new EntityField("lastUpdated", "Last Updated",
                "Date of last update to this account", true, "date"));
        entity.setFields(fields);
        entities.add(entity);

        // Account
        entity = new DomainEntity();
        entity.setName("Account");
        entity.setDescription("An account is associated with zero to many Notes and zero to many Documents. It typically has one Contact though may have more.");
        entity.setImageUrl("images/domain/account-context.png");
        fields = new ArrayList<EntityField>();
        fields.add(new EntityField("name", "Name",
                "Name of the company or organisation", true, "text"));
        fields.add(new EntityField(
                "companyNumber",
                "Company Number",
                "The number for this company issued by the registrar of companies in your country.",
                false, "number"));
        fields.add(new EntityField("aliases", "Also known as",
                "Other names for the company such as names you trade as",
                false, "text"));
        fields.add(new EntityField("businessWebsite", "Business Website",
                "The primary website for the business", false, "url"));
        fields.add(new EntityField("shortDesc", "Short Description",
                "Brief description of the business", false, "text"));
        fields.add(new EntityField("description", "Description",
                "A fuller description", false, "text"));
        fields.add(new EntityField("incorporationYear", "Established In",
                "The year the business was incorporated", false, "number"));
        fields.add(new EntityField("noOfEmployees", "No. of Employees",
                "The number of full time staff you employee", false, "number"));
        fields.add(new EntityField("tenantId", "Tenant",
                "Name of the the Omny account", true, "text"));
        fields.add(new EntityField("firstContact", "First Contact",
                "Date of first contact with this business", true, "date"));
        fields.add(new EntityField("lastUpdated", "Last Updated",
                "Date of last update to this account", true, "date"));
        entity.setFields(fields);
        entities.add(entity);

        // Activity
        entity = new DomainEntity();
        entity.setName("Activity");
        entity.setDescription("An Activity is associated with exactly one Contact.");
        entity.setImageUrl("images/domain/activity-context.png");
        fields = new ArrayList<EntityField>();
        fields.add(new EntityField(
                "type", "Type",
                "Type of activity, for example: register, login, download etc.",
                false, "text"));
        fields.add(new EntityField(
                "content", "Content",
                "Additional content dependent on the type, for example for a download this will hold what was downloaded",
                true, "text"));
        fields.add(new EntityField("occurred", "Occurred",
                "Date and time this activity occurred", true, "date"));
        entity.setFields(fields);
        entities.add(entity);

        // Note
        entity = new DomainEntity();
        entity.setName("Note");
        entity.setDescription("A note is associated with exactly one Contact.");
        entity.setImageUrl("images/domain/note-context.png");
        fields = new ArrayList<EntityField>();
        fields.add(new EntityField("author", "Author",
                "Name / username of the person creating this note", true,
                "text"));
        fields.add(new EntityField("content", "Content",
                "The body of the note.", false, "text"));
        fields.add(new EntityField("created", "Created",
                "Date this note was entered", true, "date"));
        entity.setFields(fields);
        entities.add(entity);

        // Document
        entity = new DomainEntity();
        entity.setName("Document");
        entity.setDescription("A document is associated with exactly one Contact.");
        entity.setImageUrl("images/domain/document-context.png");
        fields = new ArrayList<EntityField>();
        fields.add(new EntityField("author", "Author",
                "Name / username of the person creating this document", true,
                "text"));
        fields.add(new EntityField("url", "URL",
                "The location of the document.", false, "text"));
        fields.add(new EntityField("created", "Created",
                "Date this document was entered", true, "date"));
        entity.setFields(fields);
        entities.add(entity);

        // Users
        entity = new DomainEntity();
        entity.setName("User");
        entity.setDescription("A User of the system who may belong to one or more groups and who may be allocated work.");
        entity.setImageUrl("images/domain/user-context.png");
        fields = new ArrayList<EntityField>();
        fields.add(new EntityField("firstName", "First Name",
                "Your first or given name", true, "text"));
        fields.add(new EntityField("lastName", "Last Name",
                "Your last or family name", true, "text"));
        fields.add(new EntityField("email", "Email Address",
                "Your business email address", true, "text"));
        entity.setFields(fields);
        entities.add(entity);

        // Email actions
        entity = new DomainEntity();
        entity.setName("Email");
        entity.setDescription("An email is an action (conclusion) available to the decision table authors.");
        entity.setImageUrl("images/domain/email-context.png");
        fields = new ArrayList<EntityField>();
        fields.add(new EntityField("subjectLine", "Subject Line",
                "Subject line for the email", true, "text"));
        fields.add(new EntityField("templateName", "Template Name",
                "Name of email template to use", true, "text"));
        entity.setFields(fields);
        entities.add(entity);

        model.setEntities(entities);
        return model;
    }

    private DomainModel getFirmGainsDomain() {
        DomainModel model = getDefaultDomain();

        // Contact
        List<EntityField> fields = model.getEntities().get(0).getFields();
        fields.add(new CustomEntityField("shareOfBusiness",
                "Share of Business (%)", "Your share in the business.", false,
                "number"));

        // Account
        fields = model.getEntities().get(1).getFields();
        fields.add(new CustomEntityField("alreadyContacted",
                "Already Contacted",
                "Any brokers or purchasers you are already in contact with",
                false, "text"));
        fields.add(new CustomEntityField("ebitda", "EBITDA",
                "Earnings before interest, tax, debts and adjustments", false,
                "number"));
        fields.add(new CustomEntityField("surplus", "Surplus",
                "Surplus cash and investments", false, "number"));
        fields.add(new CustomEntityField("depreciationAmortisation",
                "Depreciation / Amortisation", "", false, "number"));
        fields.add(new CustomEntityField("operatingProfit", "Operating Profit",
                "Gross profits", false, "number"));
        fields.add(new CustomEntityField("adjustments", "Adjustments", "",
                false, "number"));
        fields.add(new CustomEntityField("borrowing", "Borrowing", "", false,
                "number"));
        fields.add(new CustomEntityField("lowQuote", "Low quote", "", false,
                "number"));
        fields.add(new CustomEntityField("mediumQuote", "Medium quote", "",
                false, "number"));
        fields.add(new CustomEntityField("highQuote", "High quote", "", false,
                "number"));
        fields.add(new CustomEntityField("askingPrice", "Asking price", "",
                false, "number"));
        // fields.add(new CustomEntityField());
        // fields.add(new CustomEntityField());
        // fields.add(new CustomEntityField());
        // fields.add(new CustomEntityField());
        // fields.add(new CustomEntityField());

        return model;
    }

}
