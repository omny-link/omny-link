package com.knowprocess.bpm;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.pages.PageObject;

import org.openqa.selenium.WebElement;

@DefaultUrl("http://www.etsy.com")
public class HomePage extends PageObject {

	@FindBy(css = "button[value='Search']")
	WebElement searchButton;

	public void searchFor(String keywords) {
		$("#search-query").sendKeys(keywords);
		searchButton.click();
	}
}