package com.knowprocess.in.filters;

import com.google.code.linkedinapi.schema.Person;
import com.knowprocess.in.PersonFilter;

public class MatchAllFilter implements PersonFilter {

	@Override
	public boolean match(Person c) {
		return true;
	}

}
