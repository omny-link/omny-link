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
package com.knowprocess.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvData implements Serializable {

	private static final long serialVersionUID = -4117113623921324904L;

	private static final String FIELD_SEPARATOR = ",";

	private List<Map<String, String>> records;

	public CsvData parse(String data) {
		this.records = new ArrayList<Map<String, String>>();

		String[] lines = data.split("\n");
		String[] keys = lines[0].split(FIELD_SEPARATOR);

		for (int i = 1; i < lines.length; i++) {
			Map<String, String> map = new HashMap<String, String>();
			String[] record = lines[i].split(FIELD_SEPARATOR);
			for (int j = 0; j < record.length; j++) {
				map.put(unwrap(keys[j]), unwrap(record[j]));
			}
			records.add(map);
		}
		return this;
	}

	private String unwrap(String s) {
		if (s.startsWith("\"")) {
			return s.substring(1, s.length() - 1);
		} else {
			return s;
		}
	}

	public String get(int idx, String key) {
		return records.get(idx).get(key);
	}

	@Override
	public String toString() {
		return "CsvData: " + records;
	}
}
