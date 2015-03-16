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
        results.put("lowQuote", (3 * ebitda) + netDebt);
        results.put("mediumQuote", (5 * ebitda) + netDebt);
        results.put("highQuote", (7 * ebitda) + netDebt);
        return results;
    }

    private Double nullToZero(Double n) {
        return n == null ? 0d : n;
    }
}
