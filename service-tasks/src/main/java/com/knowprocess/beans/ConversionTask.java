package com.knowprocess.beans;

import org.springframework.core.convert.support.GenericConversionService;

public class ConversionTask {

    private GenericConversionService conversionService;

    public ConversionTask() {
        conversionService = new GenericConversionService();
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(Object source, Class<?> targetType) {
        return (T) conversionService.convert(source, targetType);
    }

}
