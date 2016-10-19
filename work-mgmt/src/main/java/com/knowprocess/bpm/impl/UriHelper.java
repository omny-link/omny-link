package com.knowprocess.bpm.impl;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

public class UriHelper {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(UriHelper.class);

    protected static URI getGlobalUri(Class<?> controllerClass, String id) {
        try {
            UriComponentsBuilder builder = MvcUriComponentsBuilder
                    .fromController(controllerClass);
            String uri = builder.build().toUriString()
                    .replace("{tenantId}/", "");
            return new URI(uri);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String expandUri(Class<?> controllerClass, String varName,
            String varValue) {
        // TODO a generalisation based on annotation scanning?

        String globalUri = getGlobalUri(controllerClass, varValue).toString();
        if (globalUri.endsWith("/")) {
            globalUri = globalUri.substring(0, globalUri.lastIndexOf("/"));
        }
        switch (varName) {
        case "accountId":
            varValue = String.format("%1$s/accounts/%2$s", globalUri, varValue);
            break;
        case "contactId":
            varValue = String.format("%1$s/contacts/%2$s", globalUri, varValue);
            break;
        case "documentId":
            varValue = String
                    .format("%1$s/documents/%2$s", globalUri, varValue);
            break;
        case "notesId":
            varValue = String.format("%1$s/notes/%2$s", globalUri, varValue);
            break;
        }
        return varValue;
    }
}
