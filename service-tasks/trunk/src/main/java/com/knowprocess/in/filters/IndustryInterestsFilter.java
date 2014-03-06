package com.knowprocess.in.filters;

import java.util.regex.Pattern;

import com.google.code.linkedinapi.schema.Person;
import com.knowprocess.in.PersonFilter;

public class IndustryInterestsFilter implements PersonFilter {

	protected Pattern pattern;

	public IndustryInterestsFilter(String regex) {
		pattern = Pattern.compile(regex);
	}

	@Override
	public boolean match(Person c) {
		return pattern.matcher(c.getIndustry() == null ? "" : c.getIndustry())
				.matches()
				|| pattern.matcher(
						c.getInterests() == null ? "" : c.getInterests())
						.matches();
	}

}
