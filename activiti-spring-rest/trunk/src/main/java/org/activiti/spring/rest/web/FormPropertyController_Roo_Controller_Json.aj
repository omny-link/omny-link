// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.activiti.spring.rest.web;

import java.util.List;
import org.activiti.spring.rest.model.FormProperty;
import org.activiti.spring.rest.web.FormPropertyController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

privileged aspect FormPropertyController_Roo_Controller_Json {
    
    @RequestMapping(value = "/{id_}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> FormPropertyController.showJson(@PathVariable("id_") Long id_) {
        FormProperty formProperty = FormProperty.findFormProperty(id_);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        if (formProperty == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(formProperty.toJson(), headers, HttpStatus.OK);
    }
    
    @RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> FormPropertyController.listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        try {
            List<FormProperty> result = FormProperty.findAllFormPropertys();
            return new ResponseEntity<String>(FormProperty.toJsonArray(result), headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> FormPropertyController.createFromJson(@RequestBody String json, UriComponentsBuilder uriBuilder) {
        FormProperty formProperty = FormProperty.fromJsonToFormProperty(json);
        formProperty.persist();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            RequestMapping a = (RequestMapping) getClass().getAnnotation(RequestMapping.class);
            headers.add("Location",uriBuilder.path(a.value()[0]+"/"+formProperty.getId().toString()).build().toUriString());
            return new ResponseEntity<String>(headers, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> FormPropertyController.createFromJsonArray(@RequestBody String json) {
        for (FormProperty formProperty: FormProperty.fromJsonArrayToFormPropertys(json)) {
            formProperty.persist();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            return new ResponseEntity<String>(headers, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @RequestMapping(value = "/{id_}", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> FormPropertyController.updateFromJson(@RequestBody String json, @PathVariable("id_") Long id_) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        FormProperty formProperty = FormProperty.fromJsonToFormProperty(json);
        formProperty.setId_(id_);
        if (formProperty.merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/{id_}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> FormPropertyController.deleteFromJson(@PathVariable("id_") Long id_) {
        FormProperty formProperty = FormProperty.findFormProperty(id_);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            if (formProperty == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
            formProperty.remove();
            return new ResponseEntity<String>(headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
