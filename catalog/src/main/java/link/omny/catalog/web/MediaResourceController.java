/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
package link.omny.catalog.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import link.omny.catalog.model.MediaResource;
import link.omny.catalog.repositories.MediaResourceRepository;
import link.omny.catalog.views.MediaResourceViews;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import link.omny.supportservices.internal.NullAwareBeanUtils;

/**
 * REST web service for accessing stock items.
 *
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/media")
@Api(tags = "Media API")
public class MediaResourceController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MediaResourceController.class);

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MediaResourceRepository mediaResourceRepo;

    /**
     * @return a complete mediaResource including its items and feedback.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @JsonView(MediaResourceViews.Detailed.class)
    @Transactional
    @ApiOperation(value = "Retrieve the specified media resource.")
    public @ResponseBody EntityModel<MediaResource> readMediaResource(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long mediaResourceId) {
        LOGGER.info("Read mediaResource {} for tenant {}", mediaResourceId,
                tenantId);

        return addLinks(tenantId, findById(tenantId, mediaResourceId));
    }

    protected MediaResource findById(final String tenantId, final Long id) {
        return mediaResourceRepo.findById(id)
                .orElseThrow(() -> new BusinessEntityNotFoundException(
                        MediaResource.class, id));
    }

    /**
     * @return the created mediaResource.
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ApiOperation(value = "Create a new media resource.")
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody MediaResource mediaResource) {

        mediaResourceRepo.save(mediaResource);

        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", mediaResource.getId().toString());

        return getCreatedResponseEntity("/{id}", vars);
    }

    protected ResponseEntity<Object> getCreatedResponseEntity(String path, Map<String, String> vars) {
        URI location = MvcUriComponentsBuilder.fromController(getClass()).path(path).buildAndExpand(vars).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<Object>(headers, HttpStatus.CREATED);
    }

    /**
     * Update an existing mediaResource.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    @Transactional
    @ApiOperation(value = "Update the specified media resource.")
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long mediaResourceId,
            @RequestBody MediaResource updatedMediaResource) {
        MediaResource mediaResource = findById(tenantId, mediaResourceId);

        NullAwareBeanUtils.copyNonNullProperties(updatedMediaResource, mediaResource, "id", "documents", "notes", "stockItem");
        mediaResourceRepo.save(mediaResource);
    }

    /**
     * Delete an existing mediaResource.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{mediaResourceId}", method = RequestMethod.DELETE)
    @Transactional
    @ApiOperation(value = "Delete a new media resource.")
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("mediaResourceId") Long mediaResourceId) {
        mediaResourceRepo.deleteById(mediaResourceId);
    }

    protected List<EntityModel<MediaResource>> addLinks(final String tenantId, final List<MediaResource> list) {
        ArrayList<EntityModel<MediaResource>> entities = new ArrayList<EntityModel<MediaResource>>();
        for (MediaResource mediaResource : list) {
            entities.add(addLinks(tenantId, mediaResource));
        }
        return entities;
    }

    protected EntityModel<MediaResource> addLinks(final String tenantId, final MediaResource mediaResource) {
        return EntityModel.of(mediaResource, linkTo(methodOn(MediaResourceController.class)
                .findById(tenantId, mediaResource.getId()))
                .withSelfRel());
    }
}
