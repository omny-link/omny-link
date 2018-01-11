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
