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
package link.omny.custmgmt.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Scanner;

import jakarta.validation.constraints.NotNull;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class ContactAvatarService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ContactAvatarService.class);

    private PNGTranscoder t = new PNGTranscoder();

    private String template;

    private File outputDir;

    public ContactAvatarService(@NotNull String outputDir) {
        this.outputDir = new File(outputDir);
        this.outputDir.mkdirs();
    }

    @SuppressWarnings("resource")
    protected String getTemplate() {
        if (template == null) {
            try (InputStream is = getClass().getResourceAsStream("/initials.svg")) {
                template = new Scanner(is).useDelimiter("\\A").next();
            } catch (IOException e) {
                ;
            }
        }
        return template;
    }

    public File create(String initials) throws Exception {
        File file = new File(outputDir, initials==null?"unknown.png":initials.toLowerCase() + ".png");
        create(initials, new FileOutputStream(file));
        return file;
    }

    public void create(@NotNull String initials, @NotNull OutputStream os) {
        TranscoderInput input;
        try {

            String s = String.format("#%02x%02x%02x",
                    (int) (Math.random() * 150), (int) (Math.random() * 150),
                    (int) (Math.random() * 150));

            String text = String.format(getTemplate(), initials.toUpperCase(),
                    s, "#b7b7b7");

            StringReader reader = new StringReader(text);
            input = new TranscoderInput(reader);

            TranscoderOutput output = new TranscoderOutput(os);

            t.transcode(input, output);
        } catch (Exception e) {
            LOGGER.error("Unable to generate gravatar for {}", initials);
            try {
                writeErrorAvatar(os);
            } catch (Exception e1) {
                LOGGER.error("Unable to create default avatar", e);
            }
        } finally {
            try {
                os.close();
            } catch (Exception e) {
            }
        }
    }

    public void writeUnknownAvatar(OutputStream oos) {
        create("??", oos);
    }

    public void writeErrorAvatar(OutputStream oos) {
        create("XX", oos);
    }
}
