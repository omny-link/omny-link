package com.knowprocess.services.encode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base64EncoderTask implements JavaDelegate {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Base64EncoderTask.class);

    /**
     * The data to encode.
     */
    protected Expression data;

    /**
     * Variable name (or expression resolving to variable) to store result.
     */
    protected Expression outputVariable;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        byte[] input = getBytes(data.getValue(execution));
        LOGGER.info("Requested to Base64 encode {} bytes at {} of {}",
                input.length, execution.getCurrentActivityId(),
                execution.getProcessDefinitionId());
        String urlEncoded = execute(input);
        LOGGER.info("Base64 encoding at {} of {} produced {} bytes",
                execution.getCurrentActivityId(),
                execution.getProcessDefinitionId(),
                urlEncoded.getBytes().length);
        LOGGER.debug("  encoded string is: {}", urlEncoded);
        execution.setVariable((String) outputVariable.getValue(execution),
                urlEncoded);
    }

    public String execute(byte[] input) throws UnsupportedEncodingException {
        try {
            java.nio.file.Files.write(
                    java.nio.file.Paths.get("/var/tmp/test-from-bpmn-mime.pdf"),
                    input, StandardOpenOption.CREATE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new ActivitiException("", e);
        }
        String encoded = Base64.getMimeEncoder().encodeToString(input);
        try {
            java.nio.file.Files.write(
                    java.nio.file.Paths.get("/var/tmp/test-from-bpmn-mime.b64"),
                    encoded.getBytes(),
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new ActivitiException("", e);
        }
        return encoded;
        // // return new String(Base64.encodeBase64URLSafe(input), "UTF-8");
        // return new String(Base64.encodeBase64(input), "UTF-8");
    }

    private byte[] getBytes(Object variable)
            throws UnsupportedEncodingException {
        if (variable == null) {
            return new byte[0];
        } else if (variable instanceof byte[]) {
            return (byte[]) variable;
        }
        return variable.toString().getBytes("UTF-8");
    }
}
