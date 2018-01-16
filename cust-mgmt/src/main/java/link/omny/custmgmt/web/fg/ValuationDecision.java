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

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ValuationDecision {

    public Map<String, Double> calc(Double operatingProfit,
            Double depreciationAmortisation,
            Double adjustments, Double ebitda, Double surplus, Double borrowing) {
        HashMap<String, Double> results = new HashMap<String, Double>();

        if (operatingProfit == null && depreciationAmortisation == null
                && adjustments == null && ebitda == null) {
            throw new IllegalStateException(
                    "Must supply financials (operatingProfit, depreciationAmortisation and adjustments) or EBITDA");
        } else if (operatingProfit != null && depreciationAmortisation != null
                && adjustments != null) {
            ebitda = operatingProfit + depreciationAmortisation + adjustments;
            results.put("ebitda", ebitda);
        }

        Double netDebt = nullToZero(surplus) - nullToZero(borrowing);
        if (ebitda.doubleValue() < 50000) {
            results.put("lowQuote", (1.45 * ebitda) + netDebt);
            results.put("mediumQuote", (2 * ebitda) + netDebt);
            results.put("highQuote", (2.65 * ebitda) + netDebt);
        } else if (ebitda.doubleValue() >= 50000 && ebitda.doubleValue() <300000) {
            results.put("lowQuote", (1.95 * ebitda) + netDebt);
            results.put("mediumQuote", (2.9 * ebitda) + netDebt);
            results.put("highQuote", (3.85 * ebitda) + netDebt);
        } else if (ebitda.doubleValue() >= 300000&& ebitda.doubleValue() < 1000000) {
            results.put("lowQuote", (2.5 * ebitda) + netDebt);
            results.put("mediumQuote", (3.75 * ebitda) + netDebt);
            results.put("highQuote", (5.5 * ebitda) + netDebt);
        } else {
            results.put("lowQuote", (3.1 * ebitda) + netDebt);
            results.put("mediumQuote", (4.95 * ebitda) + netDebt);
            results.put("highQuote", (6.85 * ebitda) + netDebt);
        }
        return results;
    }

    private Double nullToZero(Double n) {
        return n == null ? 0d : n;
    }
}
