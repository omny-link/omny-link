package com.knowprocess.in.filters;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.linkedinapi.schema.Person;
import com.knowprocess.in.PersonFilter;

public class NameFilter implements PersonFilter {
	protected static final Logger LOGGER = LoggerFactory
			.getLogger(NameFilter.class);

	protected Pattern pattern;

	public NameFilter(String regex) {
		LOGGER.debug(String.format("Looking for names matching %1$s", regex));
		if (regex == null || regex.trim().length() == 0) {
			pattern = Pattern.compile(".*");
		} else {
			pattern = Pattern.compile(regex);
		}
	}

	@Override
	public boolean match(Person c) {
		return pattern.matcher(c.getLastName()).matches()
				|| pattern.matcher(c.getFirstName()).matches();
	}

}
