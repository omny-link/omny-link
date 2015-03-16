package link.omny.custmgmt.web.fg;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class ValuationDecisionTest {

    private static ValuationDecision valuationDecision;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        valuationDecision = new ValuationDecision();
    }

    @Test
    public void testCalcByFinancials() {
        Map<String, Double> results = valuationDecision.calc(100000d, 20000d,
                3000d, null, 400d, 50d);
        assertEquals(new Double(369350), results.get("lowQuote"));
        assertEquals(new Double(615350), results.get("mediumQuote"));
        assertEquals(new Double(861350), results.get("highQuote"));
        assertEquals(new Double(123000), results.get("ebitda"));
    }

    @Test
    public void testCalcByEbitda() {
        Map<String, Double> results = valuationDecision.calc(null, null, null,
                123000d, 400d, 50d);
        assertEquals(new Double(369350), results.get("lowQuote"));
        assertEquals(new Double(615350), results.get("mediumQuote"));
        assertEquals(new Double(861350), results.get("highQuote"));
    }
}
