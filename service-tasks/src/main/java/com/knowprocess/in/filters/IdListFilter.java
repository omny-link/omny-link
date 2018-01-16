/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
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
