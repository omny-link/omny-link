package link.omny.custmgmt.web.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import link.omny.custmgmt.model.Memo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class MemoCsvConverter extends
        AbstractHttpMessageConverter<List<Memo>> {
    public static final MediaType MEDIA_TYPE = new MediaType("text", "csv");

    protected static final ArrayList<Memo> arrayList = new ArrayList<Memo>();

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MemoCsvConverter.class);

    public MemoCsvConverter() {
        super(MEDIA_TYPE);
    }

    protected boolean supports(Class<?> clazz) {
        LOGGER.debug(" Test if supports: " + clazz.getName());
        return clazz.isInstance(arrayList);
    }

    protected void writeInternal(List<Memo> list,
            HttpOutputMessage output)
            throws IOException, HttpMessageNotWritableException {
        output.getHeaders().setContentType(MEDIA_TYPE);
        output.getHeaders().set("Content-Disposition",
                "attachment; filename=\"" + Memo.class.getSimpleName()
                        + ".csv\"");
        OutputStream out = output.getBody();
        Writer writer = new OutputStreamWriter(out);
        for (Memo bean : list) {
            writer.write(bean.toCsv());
            writer.write("\n");
        }
        writer.close();
    }

    @Override
    protected List<Memo> readInternal(Class<? extends List<Memo>> clazz,
            HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        // TODO Auto-generated method stub
        return null;
    }

}
