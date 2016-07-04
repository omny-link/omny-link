package link.omny.custmgmt.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Scanner;

import lombok.NoArgsConstructor;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class ContactAvatarService {

    private PNGTranscoder t = new PNGTranscoder();

    private String template;

    private File outputDir;

    public ContactAvatarService(String outputDir) {
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
        File file = new File(initials.toLowerCase() + ".png");
        create(initials, new FileOutputStream(file));
        return file;
    }

    public void create(String initials, OutputStream os) throws Exception {
        TranscoderInput input;
        try {

            String s = String.format("#%02x%02x%02x",
                    (int) (Math.random() * 200), (int) (Math.random() * 200),
                    (int) (Math.random() * 200));

            String text = String.format(getTemplate(), initials.toUpperCase(),
                    s, "#b7b7b7");

            StringReader reader = new StringReader(text);
            input = new TranscoderInput(reader);

            TranscoderOutput output = new TranscoderOutput(os);

            t.transcode(input, output);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            os.flush();
            os.close();
        }
    }
}