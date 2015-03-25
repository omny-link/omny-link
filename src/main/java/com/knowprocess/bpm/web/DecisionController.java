package com.knowprocess.bpm.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.decision.model.DecisionExpression;
import com.knowprocess.decision.model.DecisionModel;

@Controller
@RequestMapping(value = "/{tenantId}/decision")
public class DecisionController {

    @RequestMapping(value = "/{decisionName}", method = RequestMethod.GET)
    public @ResponseBody DecisionModel getModelForTenant(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("decisionName") String decisionName) {
        switch (tenantId) {
        case "firmgains":
            switch (decisionName) {
            case "valuation":
                return getFirmGainsValuationModel();
            default:
                return getNewDecisionModel();
            }
        case "examples":
            switch (decisionName) {
            case "riskRating":
                return getRiskRatingModel();
            default:
                return getNewDecisionModel();
            }
        default:
            return getNewDecisionModel();
        }
    }

    private DecisionModel getRiskRatingModel() {
        DecisionModel model = new DecisionModel();
        model.setName("Applicant Risk Rating");

        List<DecisionExpression> conditions = new ArrayList<DecisionExpression>();
        conditions.add(new DecisionExpression("Applicant Age", new String[] {
                "<25", "<25", "[25..60]", ">60", ">60" }));
        conditions.add(new DecisionExpression("Medical History", new String[] {
                "good", "bad", "-", "good", "bad" }));
        model.setConditions(conditions);

        List<DecisionExpression> conclusions = new ArrayList<DecisionExpression>();
        conclusions.add(new DecisionExpression("Low", new String[] { "X", "-",
                "-", "-", "-" }));
        conclusions.add(new DecisionExpression("Medium", new String[] { "-",
                "X", "X", "X", "-" }));
        conclusions.add(new DecisionExpression("High", new String[] { "-", "-",
                "-", "-", "X" }));
        model.setConclusions(conclusions);
        return model;
    }

    private DecisionModel getNewDecisionModel() {
        DecisionModel model = new DecisionModel();
        model.setName("A new decision");
        List<DecisionExpression> conditions = new ArrayList<DecisionExpression>();
        model.setConditions(conditions);
        List<DecisionExpression> conclusions = new ArrayList<DecisionExpression>();
        model.setConclusions(conclusions);
        return model;
    }

    private DecisionModel getFirmGainsValuationModel() {
        DecisionModel model = new DecisionModel();
        model.setName("Simplified Business Valuation");

        List<DecisionExpression> conditions = new ArrayList<DecisionExpression>();
        conditions.add(new DecisionExpression("Turnover", new String[] {
                "<100000", ">= 100000" }));
        model.setConditions(conditions);

        List<DecisionExpression> conclusions = new ArrayList<DecisionExpression>();
        conclusions.add(new DecisionExpression("Low Quote Multiple",
                new String[] { "3", "6" }));
        conclusions.add(new DecisionExpression("Medium Quote Multiple",
                new String[] { "5", "10" }));
        conclusions.add(new DecisionExpression("High Quote Multiple",
                new String[] { "7", "14" }));
        model.setConclusions(conclusions);
        return model;
    }
}
