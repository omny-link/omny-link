package com.knowprocess.bpm;

import java.util.ArrayList;
import java.util.List;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;

import org.openqa.selenium.WebElement;

public class SearchResultsPage extends PageObject {

	@FindBy(css = ".listing-card")
	List<WebElement> listingCards;

	public List<String> getResultTitles() {
		return new ArrayList<String>();
		// return listingCards.stream()
		// .map(element -> element.getText())
		// .collect(Collectors.toList());
    }
}