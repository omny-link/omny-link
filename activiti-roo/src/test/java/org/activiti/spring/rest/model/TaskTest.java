package org.activiti.spring.rest.model;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class TaskTest {

	@Test
	public void testJsonSerialize() {
		Task task = new Task();
		List<Task> list = Collections.singletonList(task);
		Task.toJsonArray(list);
	}

}
