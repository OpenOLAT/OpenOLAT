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
package org.olat.modules.portfolio.manager;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Section;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * Initial date: 12.03.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class PortfolioServiceSearchOptions {
	
	private Binder binder;
	private Section section;
	private String searchString;
	private List<TaxonomyLevel> taxonomyLevels;
	private List<Category> categories;
	private Identity owner;
	
	public PortfolioServiceSearchOptions(Binder binder, Section section, String searchString, List<TaxonomyLevel> taxonomyLevels, List<Category> categories) {
		this.binder = binder;
		this.section = section; 
		this.searchString = searchString;
		this.taxonomyLevels = taxonomyLevels;
		this.categories = categories;
	}
	
	public Binder getBinder() {
		return binder;
	}
	
	public void setBinder(Binder binder) {
		this.binder = binder;
	}
	
	public Section getSection() {
		return section;
	}
	
	public void setSection(Section section) {
		this.section = section;
	}
	
	public String getSearchString() {
		return searchString;
	}
	
	public void setSearchString(String searchString) {
		if (StringHelper.containsNonWhitespace(searchString)) {
			this.searchString = searchString.toLowerCase();
		}
	}
	
	public List<TaxonomyLevel> getTaxonomyLevels() {
		return taxonomyLevels;
	}
	
	public void setTaxonomyLevels(List<TaxonomyLevel> taxonomyLevels) {
		this.taxonomyLevels = taxonomyLevels;
	}
	
	public List<Category> getCategories() {
		return categories;
	}
	
	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	
	public Identity getOwner() {
		return owner;
	}
	
	public void setOwner(Identity owner) {
		this.owner = owner;
	}
}
