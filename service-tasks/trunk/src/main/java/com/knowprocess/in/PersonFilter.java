package com.knowprocess.in;
import com.google.code.linkedinapi.schema.Person;
public interface PersonFilter {

	boolean match(Person c);
}
