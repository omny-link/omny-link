package com.knowprocess.activiti.sugarcrm;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.knowprocess.mail.MailData;
import com.knowprocess.sugarcrm.api.SugarNote;
import com.knowprocess.sugarcrm.api.SugarSession;

/**
 * Add a Note to the Lead record in Sugar CRM from an Activiti service task.
 * 
 * @author tstephen
 */
public class RecordMailToLeadTask extends SugarTask implements JavaDelegate {

	public RecordMailToLeadTask() {
		super();
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		SugarSession session = doSugarUserLogin(execution, svc);

		MailData mail = (MailData) execution.getVariable("mailData");
		SugarNote note = new SugarNote("Mailshot", mail.get("surveyUrl"));
//				(String) execution.getVariable("mailText"));
		System.out.println("note:" + note.getNameValueListAsJson());
		svc.addNoteToLead(session, mail.get("addressee.id"), note);
		System.out.println(String.format(
				"Attached note with id '%1$s' lead with id '%2$s'",
				note.getId(), mail.get("addressee.id")));
		List<SugarNote> notes = (List<SugarNote>) execution
				.getVariable("sugarNotes");
		if (notes == null) {
			notes = new ArrayList<SugarNote>();
		}
		notes.add(note);
		execution.setVariable("sugarNotes", notes);
	}

}
