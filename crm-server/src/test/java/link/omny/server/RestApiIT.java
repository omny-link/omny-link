package link.omny.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestApiIT {

    private static ScheduledExecutorService globalScheduledThreadPool = Executors.newScheduledThreadPool(20);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testAccountApi() throws IOException {
        StringBuilder sb = createScript(
                "classpath:META-INF/resources/webjars/jasmine-boot/1.0.0/js/rest-helper.js",
                "classpath:META-INF/resources/webjars/custmgmt/3.0.0/specs/AcctMgmtSpec.js" );

        JsonNode report = runScript(sb);
        assertEquals(10, report.get("suite").get("totalSpecsDefined").asInt());
        assertNoFailedExpectations(report);
    }

    @Test
    public void testContactApi() throws IOException {
        StringBuilder sb = createScript(
                "classpath:META-INF/resources/webjars/jasmine-boot/1.0.0/js/rest-helper.js",
                "classpath:META-INF/resources/webjars/custmgmt/3.0.0/specs/CustMgmtSpec.js" );

        JsonNode report = runScript(sb);
        assertEquals(14, report.get("suite").get("totalSpecsDefined").asInt());
        assertNoFailedExpectations(report);
    }

    @Test
    public void testOrderApi() throws IOException {
        StringBuilder sb = createScript(
                "classpath:META-INF/resources/webjars/jasmine-boot/1.0.0/js/rest-helper.js",
                "classpath:META-INF/resources/webjars/catalog/3.0.0/specs/OrderSpec.js" );

        JsonNode report = runScript(sb);
        assertEquals(12, report.get("suite").get("totalSpecsDefined").asInt());
        assertNoFailedExpectations(report);
    }

    private void assertNoFailedExpectations(JsonNode report) {
        for (Iterator<JsonNode> it = report.get("results").elements() ; it.hasNext() ; ) {
            JsonNode result = (JsonNode) it.next();
            assertEquals("Spec failed: " + result.get("fullName").asText(),
                    0, result.get("failedExpectations").size());
        }
    }

    private JsonNode runScript(StringBuilder sb) throws IOException {
        ScriptEngine engine = getEngine();
        try {
            engine.eval(sb.toString());

            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            String report = (String) bindings.get("report");
            return objectMapper.readTree(report);
        } catch (ScriptException e) {
            e.printStackTrace();
            fail();
            return null;
        }
    }

    private void loadPolyfills(StringBuilder sb) {
        sb.append("load(\"classpath:META-INF/resources/webjars/jasmine-boot/1.0.0/js/timer-polyfill.js\");\n");
        sb.append("load(\"classpath:META-INF/resources/webjars/jasmine-boot/1.0.0/js/xml-http-request-polyfill.js\");\n");
    }

    private StringBuilder createScript(String... scripts) {
        StringBuilder sb = new StringBuilder();
        sb.append("var window = this;\n");

        loadPolyfills(sb);
        loadJasmine(sb);
        sb.append(String.format("load(\"%1$s\");", "classpath:META-INF/resources/webjars/jasmine-boot/1.0.0/js/json-reporter.js"));

        for (String script : scripts) {
            sb.append(String.format("load(\"%1$s\");", script));
        }

        sb.append("jasmine.getEnv().execute();\n");

        return sb;
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

    private ScriptEngine getEngine() {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("nashorn");

        //Injection of __NASHORN_POLYFILL_TIMER__ in ScriptContext
        engine.getContext().setAttribute("__NASHORN_POLYFILL_TIMER__", globalScheduledThreadPool, ScriptContext.ENGINE_SCOPE);
        engine.getContext().setWriter(new PrintWriter(System.out));
        return engine;
    }
}
