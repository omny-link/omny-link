package link.omny.custmgmt.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Scanner;

import javax.validation.constraints.NotNull;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ContactAvatarService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ContactAvatarService.class);
    
    private PNGTranscoder t = new PNGTranscoder();

    private String template;

    private File outputDir;

    public ContactAvatarService() {
        // Jasper Reports baulks if we include Xerces and Batik if we don't
        // From
        // http://mail-archives.apache.org/mod_mbox/xmlgraphics-batik-users/200501.mbox/%3C3AE413A5D1F1D44188D9D2810E67186FD41206@MOEEXC02.europe.bmw.corp%3E
        t.addTranscodingHint(PNGTranscoder.KEY_XML_PARSER_CLASSNAME,
                "com.sun.org.apache.xerces.internal.parsers.SAXParser");
        t.addTranscodingHint(PNGTranscoder.KEY_XML_PARSER_VALIDATING,
                new Boolean(false));
        t.addTranscodingHint(PNGTranscoder.KEY_DOM_IMPLEMENTATION,
                SVGDOMImplementation.getDOMImplementation());
    }

    public ContactAvatarService(@NotNull String outputDir) {
        this();
        this.outputDir = new File(outputDir);
        this.outputDir.mkdirs();
    }

    protected String getTemplate() {
        if (template == null) {
            InputStream is = null;
            try {
                is = getClass().getResourceAsStream("/initials.svg");

                template = new Scanner(is).useDelimiter("\\A").next();
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                    ;
                }
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
            LOGGER.error(String.format("Unable to generate gravatar for %1$s", initials));
            try {
                create("??", os);
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
}