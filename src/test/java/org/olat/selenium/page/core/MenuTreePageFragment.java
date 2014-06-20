/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.core;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.junit.Assert;
import org.olat.selenium.page.BusyPredicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Fragment which contains the menu tree. The WebElement to create
 * this fragment must be a parent of the div.o_tree
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MenuTreePageFragment {
	
	@FindBy(className="o_tree")
	private WebElement tree;
	
	/**
	 * Click the root link in the tree.
	 * 
	 * @return The menu page fragment
	 */
	public MenuTreePageFragment selectRoot() {
		List<WebElement> rootLinks = tree.findElements(By.cssSelector("a.o_tree_link"));
		Assert.assertNotNull(rootLinks);
		Assert.assertFalse(rootLinks.isEmpty());
		
		rootLinks.get(0).click();
		Graphene.waitModel().until(new BusyPredicate());
		return this;
	}

}
