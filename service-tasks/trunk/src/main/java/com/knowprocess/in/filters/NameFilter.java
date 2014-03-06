package com.knowprocess.in.filters;

import java.util.regex.Pattern;

import com.google.code.linkedinapi.schema.Person;
import com.knowprocess.in.PersonFilter;

public class NameFilter implements PersonFilter {

	protected Pattern pattern;

	public NameFilter(String regex) {
		pattern = Pattern.compile(regex);
	}

	@Override
	public boolean match(Person c) {
		return pattern.matcher(c.getLastName()).matches()
				|| pattern.matcher(c.getFirstName()).matches();
	}

}
