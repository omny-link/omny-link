package link.omny.acctmgmt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TenantConfigTest {

    private static final String CONFIG = "/static/tenants/test.json";
    private static final String LEGACY_CONFIG = "/static/tenants/legacyTenantConfig.json";
    private static ObjectMapper objectMapper;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testDeserialise() throws JsonParseException,
            JsonMappingException, IOException {
        TenantConfig config = objectMapper.readValue(
                TenantConfig.readResource(CONFIG),
                new TypeReference<TenantConfig>() {
                });
        System.out.println(String.format("  found %1$s tenant config",
                config.getId()));
        assertEquals(1, config.getContactActions().size());
        assertEquals(11, config.getToolbar().size());
        assertEquals(6, config.getPartials().size());
        assertEquals(1, config.getProcesses().size());
        assertEquals(1, config.getTypeaheadControls().size());
        assertEquals(5, config.getTypeaheadControls().get(0).getValues().size());
        for (TenantTypeaheadValue value : config.getTypeaheadControls().get(0)
                .getValues()) {
            assertNotNull(value.getIdx());
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(
                    "target/test-serialization-tenant-config.json");
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(fos, config);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            fos.close();
        }
    }

    @Test
    public void testDeserialiseLegacy() throws JsonParseException,
            JsonMappingException, IOException {
        TenantConfig config = objectMapper.readValue(
                TenantConfig.readResource(LEGACY_CONFIG),
                new TypeReference<TenantConfig>() {
                });
        assertEquals(2, config.getContactFields().size());
        assertEquals(32, config.getAccountFields().size());
        assertEquals(7, config.getToolbar().size());
        assertEquals(8, config.getPartials().size());
        assertEquals(5, config.getContactActions().size());
        assertEquals(1, config.getProcesses().size());
        assertEquals(12, config.getTypeaheadControls().size());
        System.out.println(String.format("  found %1$s tenant config",
                config.getId()));
    }

    @Test
    public void testDeserialiseSdu() throws JsonParseException,
            JsonMappingException, IOException {
        TenantConfig config = objectMapper.readValue(
                TenantConfig.readResource("/static/tenants/sdu.json"),
                new TypeReference<TenantConfig>() {
                });
        System.out.println(String.format("  found %1$s tenant config",
                config.getId()));
        assertEquals(0, config.getContactActions().size());
        assertEquals(7, config.getToolbar().size());
        assertEquals(2, config.getPartials().size());
        assertEquals(1, config.getProcesses().size());
        assertEquals(10, config.getTypeaheadControls().size());
    }

    @Test
    public void testSerialiseEmbeddedTypeaheads() {
        TenantConfig config = new TenantConfig();
        List<TenantTypeaheadControl> typeaheadControls = new ArrayList<TenantTypeaheadControl>();

        TenantTypeaheadControl control = new TenantTypeaheadControl();
        control.setName("test");
        List<TenantTypeaheadValue> typeaheadValues = new ArrayList<TenantTypeaheadValue>();
        typeaheadValues.add(new TenantTypeaheadValue());
        control.setValues(typeaheadValues);
        typeaheadControls.add(control);

        config.setTypeaheadControls(typeaheadControls);
    }

}
