/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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
package link.omny.custmgmt.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import link.omny.custmgmt.model.Memo;
import link.omny.custmgmt.repositories.MemoRepository;
import link.omny.custmgmt.repositories.MemoSignatoryRepository;
import link.omny.custmgmt.services.ElTemplateFiller;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import link.omny.supportservices.internal.NullAwareBeanUtils;
import springfox.documentation.annotations.ApiIgnore;

/**
 * REST web service for uploading and accessing a file of JSON memos (over
 * and above the CRUD offered by spring data).
 *
 * @author Tim Stephenson
 */
@RestController
@RequestMapping(value = "/{tenantId}/memos")
@Api(tags = "Memo API")
public class MemoController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MemoController.class);

    @Autowired
    private MemoRepository memoRepo;

    @Autowired
    private MemoSignatoryRepository memoSignatoryRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ElTemplateFiller templateSvc;

    /**
     * Imports JSON representation of memos.
     *
     * <p>
     * This is a handy link: http://shancarter.github.io/mr-data-converter/
     *
     * @param file
     *            A file posted in a multi-part request
     * @return The meta data of the added model
     * @throws IOException
     *             If cannot parse the JSON.
     */
    @PostMapping(value = "/upload")
    @ApiIgnore
    public @ResponseBody Iterable<Memo> handleFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info("Uploading memos for: {}", tenantId);
        String content = new String(file.getBytes());

        List<Memo> list = objectMapper.readValue(content,
                new TypeReference<List<Memo>>() {
                });
        LOGGER.info("  found {} memos", list.size());
        for (Memo message : list) {
            message.setTenantId(tenantId);
        }

        Iterable<Memo> result = memoRepo.saveAll(list);
        LOGGER.info("  saved.");
        return result;
    }

    /**
     * Create a new memo.
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/")
    @ApiOperation(value = "Create a new memo.")
    public @ResponseBody ResponseEntity<Void> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Memo memo) {
        memo.setTenantId(tenantId);

        EntityModel<Memo> entity = addLinks(tenantId, memoRepo.save(memo));
        LOGGER.debug("Created memo {}", entity.getLink("self"));

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(entity.getLink("self").get().toUri());

        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }

    /**
     * Return just the memos for a specific tenant.
     *
     * @return memos for that tenant.
     */
    @GetMapping(value = "/",  produces = { "application/json" })
    @ApiOperation(value = "List a tenant's memos.")
    public @ResponseBody List<EntityModel<Memo>> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info("List memos for tenant {}", tenantId);
        return addLinks(tenantId, listAsCsv(tenantId, page, limit));
    }

    /**
     * @return memos for the specified tenant and status.
     */
    @GetMapping(value = "/findByStatus/{status}")
    @ApiOperation(value = "Find memos with the specified status.")
    public @ResponseBody List<EntityModel<Memo>> findByStatusForTenant(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("status") String status,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info("List memos with status {} for tenant {}", status, tenantId);

        List<Memo> list;
        if (limit == null) {
            list = memoRepo.findByStatusForTenant(status.toLowerCase(),
                    tenantId);
        } else {
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = memoRepo.findPageByStatusForTenant(
                    status.toLowerCase(), tenantId, pageable);
        }
        LOGGER.info("Found {} memos", list.size());
        return addLinks(tenantId, list);
    }

    protected Memo findById(final String tenantId, final Long id) {
        return memoRepo.findById(id)
                .orElseThrow(() -> new BusinessEntityNotFoundException(
                        Memo.class, id));
    }

    /**
     * Return just the matching memo.
     *
     * @param idOrName
     *            If a number will be assumed to be the id, otherwise the name.
     * @return memo for that tenant with the matching name or id.
     * @throws BusinessEntityNotFoundException
     */
    @GetMapping(value = "/{idOrName}")
    @ApiOperation(value = "Find the specified memo.")
    public @ResponseBody EntityModel<Memo> findEntityById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("idOrName") String idOrName) {
        LOGGER.debug("Find memo {}", idOrName);

        try {
            return addLinks(tenantId, findById(tenantId, Long.parseLong(idOrName)));
        } catch (NumberFormatException e) {
            return addLinks(tenantId, memoRepo.findByName(idOrName, tenantId)
                    .orElseThrow(() -> new BusinessEntityNotFoundException(
                            Memo.class, idOrName)));
        }
    }

    /**
     * Clone the specified memo.
     *
     * @param idOrName
     *            If a number will be assumed to be the id, otherwise the name.
     * @return memo for that tenant with the matching name or id.
     * @throws BusinessEntityNotFoundException
     */
    @PostMapping(value = "/{idOrName}/clone")
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Clone an existing memo, resetting fields as necessary.")
    public @ResponseBody ResponseEntity<EntityModel<Memo>> clone(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("idOrName") String idOrName) {
        LOGGER.debug("Clone memo {}", idOrName);

        Memo memo;
        try {
            memo = findById(tenantId, Long.parseLong(idOrName));
        } catch (NumberFormatException e) {
            memo = memoRepo.findByName(idOrName, tenantId)
                    .orElseThrow(() -> new BusinessEntityNotFoundException(
                            Memo.class, idOrName));
        }
        Memo resource = new Memo();
        BeanUtils.copyProperties(memo, resource, "id");
        resource.setName(memo.getName() + "Copy").setStatus("Draft");
        EntityModel<Memo> entity = addLinks(tenantId, memoRepo.save(resource));

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(entity.getLink("self").get().toUri());

        return new ResponseEntity<EntityModel<Memo>>(entity, headers, HttpStatus.CREATED);
    }

    /**
     * @return Export all memos for the specified tenant.
     */
    @GetMapping(value = "/", produces = { "text/csv" })
    @ApiOperation(value = "Export a tenant's memos.")
    public @ResponseBody List<Memo> listAsCsv(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info("Export memos for tenant {}", tenantId);

        List<Memo> list;
        if (limit == null) {
            list = memoRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = memoRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info("Found {} memos", list.size());
        return list;
    }

    /**
     * Update an existing memo.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{id}", consumes = { "application/json" })
    @Transactional
    @ApiOperation(value = "Update an existing memo.")
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long memoId,
            @RequestBody Memo updatedMemo) {
        memoSignatoryRepo.deleteAllForMemo(updatedMemo.getId());
        Memo memo = findById(tenantId, memoId);
        NullAwareBeanUtils.copyNonNullProperties(updatedMemo, memo, "id", "signatories");
        memo.addAllSignatories(updatedMemo.getSignatories());
        memo.setTenantId(tenantId);
        memo = memoRepo.save(memo);
    }

    /**
     * Delete an existing memo.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}")
    @ApiOperation(value = "Delete the specified memo.")
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long memoId) {
        memoRepo.deleteById(memoId);
    }

    /**
     * Evaluate a memo template using the provided data.
     * @throws NoSuchMethodException
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @PostMapping(value = "/eval/{memoName}",
            consumes= MediaType.APPLICATION_JSON_VALUE,
            produces = "text/html")
    @ApiOperation(value = "Evaluate a memo template.")
    public @ResponseBody ResponseEntity<String> evalJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("memoName") String memoName,
            @RequestBody String body)
            throws JsonMappingException, JsonProcessingException, NoSuchMethodException {
        LOGGER.info("eval memo {} for {} with json payload: {}",
                memoName, tenantId, body);
        Memo template = memoRepo.findByName(memoName, tenantId)
                .orElseThrow(() -> new BusinessEntityNotFoundException(Memo.class, memoName));

        Map<String, Object> parsedParams = new HashMap<String, Object>();
        JsonNode jsonNode = objectMapper.readTree(body);
        for (Iterator<Entry<String, JsonNode>> it = jsonNode.fields() ; it.hasNext() ; ) {
            Entry<String, JsonNode> entry = it.next();
            parsedParams.put(entry.getKey(), entry.getValue());
        }

        String result = templateSvc.evaluateTemplate(
                template.getRichContent(), parsedParams);
        return new ResponseEntity<String>(result, HttpStatus.OK);
    }

    /**
     * Evaluate a memo template using the provided data.
     * @throws NoSuchMethodException
     */
    @PostMapping(value = "/eval/{memoName}",
            consumes= MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = "text/html")
    @ApiOperation(value = "Evaluate a memo template.")
    public @ResponseBody ResponseEntity<String> evalUrlEncoded(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("memoName") String memoName,
            @RequestParam Map<String, String> params,
            @RequestBody String body) throws NoSuchMethodException {
        LOGGER.info("eval memo {} for {} with {} params",
                memoName, tenantId, params.size());
        Memo template = memoRepo.findByName(memoName, tenantId)
                .orElseThrow(() -> new BusinessEntityNotFoundException(Memo.class, memoName));
        LOGGER.info("found params in body {}", body);
        for (String pair : Arrays.asList(body.split("&"))) {
            try {
                String k = URLDecoder.decode(pair.substring(0, pair.indexOf('=')), "UTF-8");
                String v = URLDecoder.decode(pair.substring(pair.indexOf('=')+1), "UTF-8");

                LOGGER.info(" extracted param {}={}", k, v);
                params.put(k, v);
            } catch (UnsupportedEncodingException e) {
                String msg = String.format("Cannot decode param %1$s", pair);
                LOGGER.error(msg, e);
                throw new IllegalArgumentException(msg);
            }
        }
        Map<String, Object> parsedParams = new HashMap<String, Object>();
        for (Entry<String, String> entry : params.entrySet()) {
            LOGGER.info("  found param: {}={}", entry.getKey(), entry.getValue());
            try {
                parsedParams.put(entry.getKey(), objectMapper.readTree(entry.getValue()));
            } catch (JsonProcessingException e) {
                LOGGER.warn("  unable to parse {}, treat as simple type: {}",
                        entry.getKey(), entry.getValue());
                parsedParams.put(entry.getKey(), entry.getValue());
            }
        }
        String result = templateSvc.evaluateTemplate(
                template.getRichContent(), parsedParams);

        return new ResponseEntity<String>(result, HttpStatus.OK);
    }

    protected List<EntityModel<Memo>> addLinks(final String tenantId, final List<Memo> list) {
        ArrayList<EntityModel<Memo>> entities = new ArrayList<EntityModel<Memo>>();
        for (Memo memo : list) {
            entities.add(addLinks(tenantId, memo));
        }
        return entities;
    }

    protected EntityModel<Memo> addLinks(final String tenantId, final Memo memo) {
        return EntityModel.of(memo,
                linkTo(methodOn(MemoController.class).findEntityById(tenantId, memo.getId().toString()))
                        .withSelfRel());
    }
}
