package com.knowprocess.activiti.sugarcrm;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.knowprocess.in.LinkedInTask;
import com.knowprocess.sugarcrm.api.SugarLead;
import com.knowprocess.sugarcrm.api.SugarNote;
import com.knowprocess.sugarcrm.api.SugarSession;

/**
 * Add a Note to the Lead record in Sugar CRM from an Activiti service task.
 * 
 * @author tstephen
 */
public class RecordNoteToLeadTask extends SugarTask implements JavaDelegate {

	public RecordNoteToLeadTask() {
		super();
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		SugarSession session = doSugarUserLogin(execution, svc);

		SugarLead sugarLead = (SugarLead) execution.getVariable("sugarLead");
		String msg = (String) execution.getVariable(LinkedInTask.MESSAGE_KEY);
		msg = msg.replaceAll("\\n", "\\\\n");
		SugarNote note = new SugarNote("LinkedIn Mailshot",
				"Message as follows:\\n\\n-------------------\\n\\n" + msg);
		System.out.println("note:" + note.getNameValueListAsJson());
		svc.addNoteToLead(session, sugarLead.getId(), note);
		System.out.println(String.format(
				"Attached note with id '%1$s' to lead with id '%2$s'",
				note.getId(), sugarLead.getId()));
		List<SugarNote> notes = (List<SugarNote>) execution
				.getVariable("sugarNotes");
		if (notes == null) {
			notes = new ArrayList<SugarNote>();
		}
		notes.add(note);
		execution.setVariable("sugarNotes", notes);
	}

}
