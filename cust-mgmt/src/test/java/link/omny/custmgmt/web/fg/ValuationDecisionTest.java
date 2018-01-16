/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
        assertEquals(new Double(240200), results.get("lowQuote"));
        assertEquals(new Double(357050), results.get("mediumQuote"));
        assertEquals(new Double(473900), results.get("highQuote"));
        assertEquals(new Double(123000), results.get("ebitda"));
    }

    @Test
    public void testCalcByEbitda() {
        Map<String, Double> results = valuationDecision.calc(null, null, null,
                123000d, 400d, 50d);
        assertEquals(new Double(240200), results.get("lowQuote"));
        assertEquals(new Double(357050), results.get("mediumQuote"));
        assertEquals(new Double(473900), results.get("highQuote"));
    }
}
