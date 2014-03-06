package com.knowprocess.beans;

import org.springframework.core.convert.support.GenericConversionService;

import com.knowprocess.sugarcrm.api.SugarLead;

public class ConversionTask {


	private GenericConversionService conversionService;

	public ConversionTask() {
		conversionService = new GenericConversionService();
		conversionService.addConverter(new LinkedInPersonToSugarLead());
	}

	public SugarLead convert(Object source, Class targetType) {
		// throw new RuntimeException("Not yet implemented");
		return conversionService.convert(source, targetType);
	}

}
