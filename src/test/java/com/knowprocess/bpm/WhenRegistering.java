package com.knowprocess.bpm;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Steps;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(SerenityRunner.class)
public class WhenRegistering {
	//
	// @Steps
	// TravellerSteps travellerSteps;
	//
	// @Test
	// public void shouldCalculatePointsBasedOnDistance() {
	// // GIVEN
	// travellerSteps
	// .a_traveller_has_a_frequent_flyer_account_with_balance(10000);
	//
	// // WHEN
	// travellerSteps.the_traveller_flies(1000);
	//
	// // THEN
	// travellerSteps.traveller_should_have_a_balance_of(10100);
	//
	// }

	@Managed(driver = "firefox", uniqueSession = true)
	WebDriver driver;

	@Steps
	BuyerSteps buyer;

	@Test
	public void should_see_a_list_of_items_related_to_the_specified_keyword() {
		// GIVEN
		buyer.opens_etsy_home_page();
		// WHEN
		buyer.searches_for_items_containing("wool");
		// THEN.
		buyer.should_see_items_related_to("wool");
	}
}
