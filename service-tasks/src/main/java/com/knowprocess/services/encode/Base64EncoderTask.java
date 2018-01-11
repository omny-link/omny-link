/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
