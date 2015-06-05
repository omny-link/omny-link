package com.knowprocess.in.filters;

import java.util.Arrays;
import java.util.List;

import com.google.code.linkedinapi.schema.Person;
import com.knowprocess.in.PersonFilter;

public class IdListFilter implements PersonFilter {

	private List<String> idList;

	public IdListFilter(String ids) {
		idList = Arrays.asList(ids.split(","));
	}

	@Override
	public boolean match(Person c) {
		return idList.contains(c.getId());
	}

}
