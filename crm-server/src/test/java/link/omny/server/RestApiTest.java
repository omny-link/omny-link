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
package link.omny.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.server.web.JsEnvironmentController;

@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class RestApiTest {

    @LocalServerPort
    private String port;

    private static ScheduledExecutorService globalScheduledThreadPool = Executors.newScheduledThreadPool(20);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testAccountApi() throws IOException {
        long start = System.currentTimeMillis();
        StringBuilder sb = createScript(
                "classpath:META-INF/resources/webjars/jasmine-boot/1.1.0/js/rest-helper.js",
                "classpath:META-INF/resources/webjars/custmgmt/specs/AcctMgmtSpec.js" );

        JsonNode report = runScript(sb);
        assertEquals(16, report.get("suite").get("totalSpecsDefined").asInt());
        assertNoFailedExpectations(report);
        System.out.println(
                "Account suite took " + (System.currentTimeMillis() - start) + " (ms)");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testContactApi() throws IOException {
        long start = System.currentTimeMillis();
        StringBuilder sb = createScript(
                "classpath:META-INF/resources/webjars/jasmine-boot/1.1.0/js/rest-helper.js",
                "classpath:META-INF/resources/webjars/custmgmt/specs/ContactMgmtSpec.js" );

        JsonNode report = runScript(sb);
        assertEquals(14, report.get("suite").get("totalSpecsDefined").asInt());
        assertNoFailedExpectations(report);
        System.out.println(
                "Contact suite took " + (System.currentTimeMillis() - start) + " (ms)");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    public void testMemoApi() throws IOException {
        long start = System.currentTimeMillis();
        StringBuilder sb = createScript(
                "classpath:META-INF/resources/webjars/jasmine-boot/1.1.0/js/rest-helper.js",
                "classpath:META-INF/resources/webjars/custmgmt/specs/MemoSpec.js" );

        JsonNode report = runScript(sb);
        assertEquals(9, report.get("suite").get("totalSpecsDefined").asInt());
        assertNoFailedExpectations(report);
        System.out.println(
                " Memo suite took " + (System.currentTimeMillis() - start) + " (ms)");

    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    @Disabled
    public void testOrderApi() throws IOException {
        long start = System.currentTimeMillis();
        StringBuilder sb = createScript(
                "classpath:META-INF/resources/webjars/jasmine-boot/1.1.0/js/rest-helper.js",
                "classpath:META-INF/resources/webjars/catalog/specs/OrderSpec.js" );

        JsonNode report = runScript(sb);
        assertEquals(12, report.get("suite").get("totalSpecsDefined").asInt());
        assertNoFailedExpectations(report);
        System.out.println(
                "Order suite took " + (System.currentTimeMillis() - start) + " (ms)");

    }
    
    private void assertNoFailedExpectations(JsonNode report) {
        for (Iterator<JsonNode> it = report.get("results").elements() ; it.hasNext() ; ) {
            JsonNode result = (JsonNode) it.next();
            assertEquals(0, result.get("failedExpectations").size(),
                    "Spec failed: " + result.get("fullName").asText());
        }
    }

    private JsonNode runScript(StringBuilder sb) throws IOException {
        ScriptEngine engine = getEngine();
        try {
            engine.eval(sb.toString());

            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            String report = (String) bindings.get("report");
            return objectMapper.readTree(report);
        } catch (Throwable e) {
            System.err.println(String.format("%1$s:%2$s", e.getClass().getName(), e.getMessage()));
            e.printStackTrace();
            fail();
            return null;
        }
    }

    private void loadPolyfills(StringBuilder sb) {
        sb.append("load(\"classpath:META-INF/resources/webjars/jasmine-boot/1.1.0/js/timer-polyfill.js\");\n");
        sb.append("load(\"classpath:META-INF/resources/webjars/jasmine-boot/1.1.0/js/xml-http-request-polyfill.js\");\n");
    }

    private void loadAppEnvironment(StringBuilder sb) {
        sb.append(String.format(JsEnvironmentController.ENV, "test", ("http://localhost:" + port), "Just testing"));
    }

    private void loadReporter(StringBuilder sb) {
        sb.append(String.format("load(\"%1$s\");", "classpath:META-INF/resources/webjars/jasmine-boot/1.1.0/js/json-reporter.js"));
    }

    private void loadJasmine(final StringBuilder sb) {
        sb.append("load(\"classpath:META-INF/resources/webjars/jasmine/2.4.1/jasmine.js\");\n");
        sb.append("load(\"classpath:META-INF/resources/webjars/jasmine/2.4.1/jasmine-html.js\");\n");
        sb.append("function extend(destination, source) {\n");
        sb.append("for (var property in source) destination[property] = source[property];\n");
        sb.append("return destination;\n");
        sb.append("}\n");

        sb.append("window.jasmine = jasmineRequire.core(jasmineRequire);\n");
        sb.append("var jasmineInterface = jasmineRequire.interface(jasmine, jasmine.getEnv());\n");
        sb.append("extend(window, jasmineInterface);\n");
    }

    private StringBuilder createScript(String... scripts) {
        StringBuilder sb = new StringBuilder();
        sb.append("var window = this;\n");
        loadAppEnvironment(sb);
        loadPolyfills(sb);
        loadJasmine(sb);
        loadReporter(sb);

        for (String script : scripts) {
            sb.append(String.format("load(\"%1$s\");", script));
        }

        sb.append("jasmine.getEnv().execute();\n");

        return sb;
    }

    private ScriptEngine getEngine() {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("nashorn");

        //Injection of __NASHORN_POLYFILL_TIMER__ in ScriptContext
        engine.getContext().setAttribute("__NASHORN_POLYFILL_TIMER__", globalScheduledThreadPool, ScriptContext.ENGINE_SCOPE);
        engine.getContext().setWriter(new PrintWriter(System.out));
        return engine;
    }
}