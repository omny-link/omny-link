package link.omny.custmgmt.model;

import java.io.Serializable;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Relates a Memo Template to its recipients.
 * 
 * @author Tim Stephenson
 */
@Entity
@Table(name = "OL_MEMO_DIST")
@Data
@AllArgsConstructor
public class MemoDistribution implements Serializable {

    private static final long serialVersionUID = 1237181996013717501L;

    protected static DateFormat isoDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd");
    protected static DateFormat isoTimeFormat = new SimpleDateFormat("HH:mm");
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String owner;
    
    @JsonProperty
    @Lob
    private String recipients;

    @JsonProperty
    private String status;

    @JsonProperty
    private String memoRef;

    @Transient
    @JsonProperty
    private Date sendAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sendAtDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sendAtTime;

    /**
     * @see java.util.TimeZone.getAvailableIDs()
     */
    @JsonProperty
    private String sendAtTZ;

    /**
     * When a newsletter or other communication is sent out in bulk the mail or
     * telephony provider will typically provide a reference by which to
     * subsequently check status.
     *
     * <p>
     * For example with Mailjet this would be the campaign id.
     */
    @JsonProperty
    private String providerRef;

    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String tenantId;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP", updatable = true)
    @JsonProperty
    private Date lastUpdated;

    public MemoDistribution() {
        created = new Date();
    }

    public void addRecipients(List<Contact> contacts) {
        for (Contact contact : contacts) {
            recipients += ("," + contact.getEmail());
        }
    }

    public void removeRecipient(String recipient) {
        recipients = recipients.replace(recipient, "").replace(",,", ",");
    }

    public List<String> getRecipientList() {
        if (getRecipients() == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(getRecipients().split(","));
        }
    }

    public List<String> getRecipientTagList() {
        List<String> tags = new ArrayList<String>();
        List<String> recipients = getRecipientList();
        for (String recipient : recipients) {
            if (recipient.indexOf('@') == -1) {
                tags.add(recipient);
            }
        }
        return tags;
    }

    public void setRecipientList(List<String> recipientList) {
        if (recipientList == null || recipientList.size() == 0) {
            setRecipients(null);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String recipient : recipientList) {
            sb.append(unwrap(recipient)).append(",");
        }
        // String tmp = recipientList.toString();
        // tmp = tmp.replaceAll(", ", ",");
        setRecipients(sb.toString().substring(0, sb.toString().length() - 1));
    }

    private String unwrap(String recipient) {
        if (recipient.startsWith("\"")) {
            return recipient.substring(1, recipient.length() - 1);
        } else {
            return recipient;
        }
    }

    @PrePersist
    public void preInsert() {
        if (created == null) {
            created = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = new Date();
    }

    @JsonProperty
    public void setSendAtDate(String date) {
        if (date == null) {
            sendAtDate = null;
            return;
        }
        GregorianCalendar sendAt = new GregorianCalendar();
        if (sendAtDate != null) {
            sendAt.setTime(sendAtDate);
        }
        try {
            GregorianCalendar tmp = new GregorianCalendar();
            tmp.setTime(isoDateFormat.parse(date));
            sendAt.set(Calendar.DATE, tmp.get(Calendar.DATE));
            sendAt.set(Calendar.MONTH, tmp.get(Calendar.MONTH));
            sendAt.set(Calendar.YEAR, tmp.get(Calendar.YEAR));
            sendAtDate = sendAt.getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public String getSendAtDate() {
        return sendAtDate == null ? null : isoDateFormat.format(sendAtDate);
    }

    @JsonProperty
    public void setSendAtTime(String time) {
        if (time == null) {
            sendAtTime = null;
            return;
        }
        GregorianCalendar sendAt = new GregorianCalendar();
        if (getSendAtTime() != null) {
            sendAt.setTime(sendAtTime);
        }
        try {
            GregorianCalendar tmp = new GregorianCalendar();
            if (time.length() > 5) {
                // only want HH:mm
                time = time.substring(0, 4);
            }
            tmp.setTime(isoTimeFormat.parse(time));
            sendAt.set(Calendar.HOUR_OF_DAY, tmp.get(Calendar.HOUR_OF_DAY));
            sendAt.set(Calendar.MINUTE, tmp.get(Calendar.MINUTE));
            // these will be set zero as scheduling is not that accurate
            sendAt.set(Calendar.SECOND, tmp.get(Calendar.SECOND));
            sendAt.set(Calendar.MILLISECOND, tmp.get(Calendar.MILLISECOND));
            sendAtTime = new Time(sendAt.getTime().getTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public String getSendAtTime() {
        return sendAtTime == null ? null : isoTimeFormat.format(sendAtTime);
    }

    public Date getSendAt() {
        GregorianCalendar cal = new GregorianCalendar();
        if (getSendAtDate() == null && getSendAtTime() == null
                && getSendAtTZ() == null) {
            return null;
        }
        if (getSendAtDate() != null) {
            GregorianCalendar sendAt = new GregorianCalendar();
            sendAt.setTime(sendAtDate);
            cal.set(Calendar.DATE, sendAt.get(Calendar.DATE));
            cal.set(Calendar.MONTH, sendAt.get(Calendar.MONTH));
            cal.set(Calendar.YEAR, sendAt.get(Calendar.YEAR));
        }
        if (getSendAtTime() != null) {
            GregorianCalendar sendAt = new GregorianCalendar();
            sendAt.setTime(this.sendAtTime);
            cal.set(Calendar.HOUR_OF_DAY, sendAt.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, sendAt.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, sendAt.get(Calendar.SECOND));
            cal.set(Calendar.MILLISECOND, sendAt.get(Calendar.MILLISECOND));
        }
        if (getSendAtTZ() != null) {
            cal.setTimeZone(TimeZone.getTimeZone(getSendAtTZ()));
        }
        return cal.getTime();
    }

    public String toCsv() {
        return String.format("%1$d,%2$s,%3$s,%4$s,%5$s,%6$s", id, name, status,
                owner, memoRef, recipients);
    }

}
