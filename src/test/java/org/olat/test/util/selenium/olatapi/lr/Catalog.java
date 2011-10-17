package org.olat.test.util.selenium.olatapi.lr;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;


/**
 * Wrapper for the catalog view
 * @author Thomas Linowsky, BPS GmbH
 *
 */

public class Catalog extends OLATSeleniumWrapper{
	
	/**
	 * Default constructor
	 * @param selenium
	 */
	public Catalog(Selenium selenium){
		super(selenium);
	}
	
	/**
	 * Check whether a catalog entry with given name exists
	 * @param name The name of the resource to check for
	 * @return true if the resource exists in the catalog
	 */
	
	public boolean isEntryAvailable(String name){
		return selenium.isElementPresent("ui=learningResources::content_clickCatalogEntry(nameOfLearningResource="+name+")");
	}
	
	

}