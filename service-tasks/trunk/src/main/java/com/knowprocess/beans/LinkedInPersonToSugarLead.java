package com.knowprocess.beans;

import org.springframework.core.convert.converter.Converter;

import com.google.code.linkedinapi.schema.Person;
import com.knowprocess.sugarcrm.api.SugarLead;

public class LinkedInPersonToSugarLead implements
 Converter<Person, SugarLead> {

	@Override
	public SugarLead convert(Person o) {
		if (!(o instanceof Person)) {
			throw new IllegalArgumentException(
					"Only know how to convert LinkedIn Person to SugarLead.");
		}
		Person p = (Person) o;
		SugarLead l = new SugarLead();
		l.setFirstName(p.getFirstName());
		l.setLastName(p.getLastName());
		return l;
	}

}
