package link.omny.custmgmt.web.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import link.omny.custmgmt.model.MemoDistribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class MemoDistributionCsvConverter extends
        AbstractHttpMessageConverter<List<MemoDistribution>> {
    public static final MediaType MEDIA_TYPE = new MediaType("text", "csv");

    protected static final ArrayList<MemoDistribution> arrayList = new ArrayList<MemoDistribution>();
    
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MemoDistributionCsvConverter.class);
    
    public MemoDistributionCsvConverter() {
        super(MEDIA_TYPE);
    }

    protected boolean supports(Class<?> clazz) {
        LOGGER.debug("Test if supports: " + clazz.getName());
        return clazz.isInstance(arrayList);
    }

    protected void writeInternal(List<MemoDistribution> mailshots,
            HttpOutputMessage output)
            throws IOException, HttpMessageNotWritableException {
        output.getHeaders().setContentType(MEDIA_TYPE);
        output.getHeaders().set("Content-Disposition",
                "attachment; filename=\"" + MemoDistribution.class.getSimpleName()
                        + ".csv\"");
        OutputStream out = output.getBody();
        Writer writer = new OutputStreamWriter(out);
        for (MemoDistribution mailshot : mailshots) {
            writer.write(mailshot.toCsv());
            writer.write("\n");
        }
        writer.close();
    }

    @Override
    protected List<MemoDistribution> readInternal(
            Class<? extends List<MemoDistribution>> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        // TODO Auto-generated method stub
        return null;
    }

}
