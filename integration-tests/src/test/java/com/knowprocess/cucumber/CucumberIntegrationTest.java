package com.knowprocess.cucumber;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources", plugin = {
        /*
         * "junit:target/surefire-reports/cucumber.xml",
         * "json:target/surefire-reports/cucumber.json",
         */
        "html:target/site/cucumber" })
public class CucumberIntegrationTest {

}