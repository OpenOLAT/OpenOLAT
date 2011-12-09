/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
*/
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