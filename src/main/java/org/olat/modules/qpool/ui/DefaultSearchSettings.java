/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.qpool.ui;

import java.util.List;

import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 3 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DefaultSearchSettings {
	
	private String restrictToFormat;
	private List<QItemType> excludeTypes;
	private boolean searchAllTaxonomyLevels;
	
	private TaxonomyLevel taxonomyLevel;
	private QuestionStatus questionStatus;
	
	public DefaultSearchSettings() {
		//
	}
	
	public static final DefaultSearchSettings searchTaxonomyLevels(boolean all) {
		DefaultSearchSettings settings = new DefaultSearchSettings();
		settings.setSearchAllTaxonomyLevels(all);
		return settings;
	}
	
	public static final DefaultSearchSettings itemList(String restrictToFormat, List<QItemType> excludeTypes, boolean all) {
		DefaultSearchSettings settings = new DefaultSearchSettings();
		settings.setRestrictToFormat(restrictToFormat);
		settings.setExcludeTypes(excludeTypes);
		settings.setSearchAllTaxonomyLevels(all);
		return settings;
	}
	
	public String getRestrictToFormat() {
		return restrictToFormat;
	}
	
	public void setRestrictToFormat(String restrictToFormat) {
		this.restrictToFormat = restrictToFormat;
	}
	
	public List<QItemType> getExcludeTypes() {
		return excludeTypes;
	}
	
	public void setExcludeTypes(List<QItemType> excludeTypes) {
		this.excludeTypes = excludeTypes;
	}
	
	public boolean isSearchAllTaxonomyLevels() {
		return searchAllTaxonomyLevels;
	}
	
	public void setSearchAllTaxonomyLevels(boolean searchAllTaxonomyLevels) {
		this.searchAllTaxonomyLevels = searchAllTaxonomyLevels;
	}

	public QuestionStatus getQuestionStatus() {
		return questionStatus;
	}

	public void setQuestionStatus(QuestionStatus questionStatus) {
		this.questionStatus = questionStatus;
	}

	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	public void setTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		this.taxonomyLevel = taxonomyLevel;
	}
}
