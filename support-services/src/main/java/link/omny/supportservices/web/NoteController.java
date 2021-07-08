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
package link.omny.supportservices.web;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import link.omny.supportservices.model.Note;
import link.omny.supportservices.repositories.NoteRepository;
import springfox.documentation.annotations.ApiIgnore;

/**
 * REST web service for uploading and accessing a file of JSON Notes (over and
 * above the CRUD offered by spring data).
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/notes")
@Api(tags = "Note API")
public class NoteController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NoteController.class);

    @Autowired
    private NoteRepository noteRepo;

    @Autowired
    private ObjectMapper objectMapper;

    protected Note findById(final String tenantId, final Long id) {
        return noteRepo.findById(id)
                .orElseThrow(() -> new BusinessEntityNotFoundException(
                        Note.class, id));
    }

    /**
     * Imports JSON representation of accounts.
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
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ApiIgnore
    public @ResponseBody Iterable<Note> handleFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info(String.format("Uploading notes for: %1$s", tenantId));
        String content = new String(file.getBytes());

        List<Note> list = objectMapper.readValue(content,
                new TypeReference<List<Note>>() {
                });
        LOGGER.info(String.format("  found %1$d notes", list.size()));

        Iterable<Note> result = noteRepo.saveAll(list);
        LOGGER.info("  saved.");

        return result;
    }
    
    /**
     * Favorite an existing note.
     */
    @RequestMapping(value = "/{noteId}/favorite", method = RequestMethod.POST)
    @ApiOperation(value = "Favorite/Unfavorite the specified note.")
    public @ResponseBody void favoriteNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("noteId") Long noteId,
            @RequestParam("favorite") boolean favorite) {
        Note note = findById(tenantId, noteId);
        note.setFavorite(favorite);
        noteRepo.save(note);
    }

    /**
     * Mark an existing note as confidential.
     */
    @RequestMapping(value = "/{noteId}/confidential", method = RequestMethod.POST)
    @ApiOperation(value = "Mark/unmark the specified note as confidential.")
    public @ResponseBody void markNoteConfidential(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("noteId") Long noteId,
            @RequestParam("confidential") boolean confidential) {
        Note note = findById(tenantId, noteId);
        note.setConfidential(confidential);
        noteRepo.save(note);
    }
}
