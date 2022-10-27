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
package org.olat.modules.taxonomy.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;

/**
 * Initial date: Jan 10, 2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class TaxonomyImportContext {
	
	public static final String CONTEXT_KEY = TaxonomyImportContext.class.getSimpleName();
	
	private Taxonomy taxonomy;
	private boolean updateExistingTaxonomies;
	private List<TaxonomyLevelRow> reviewList;
	private List<TaxonomyLevel> taxonomyLevelUpdateList;
	private List<TaxonomyLevel> taxonomyLevelCreateList;
	private List<TaxonomyLevelType> taxonomyLevelTypeCreateList;
	private Map<String, List<String>> nameDescriptionByLanguage;
	private Map<TaxonomyLevel, Map<String, File>> taxonomyLevelToImage;
	
	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}
	
	public Taxonomy getTaxonomy() {
		return taxonomy;
	}
	
	public void setReviewList(List<TaxonomyLevelRow> reviewList) {
		this.reviewList = reviewList;
	}
	
	public List<TaxonomyLevelRow> getReviewList() {
		if (reviewList == null) {
			return new ArrayList<>();
		}
		return reviewList;
	}
	
	public int getUpdatedTaxonomies() {
		if (taxonomyLevelUpdateList != null) {
			return taxonomyLevelUpdateList.size();
		} else {
			return 0;
		}
	}
	
	public List<TaxonomyLevel> getTaxonomyLevelUpdateList() {
		return taxonomyLevelUpdateList;
	}
	
	public void setTaxonomyLevelUpdateList(List<TaxonomyLevel> updateList) {
		this.taxonomyLevelUpdateList = updateList;
	}
	
	public List<TaxonomyLevel> getTaxonomyLevelCreateList() {
		return taxonomyLevelCreateList;
	}
	
	public void setTaxonomyLevelCreateList(List<TaxonomyLevel> createList) {
		this.taxonomyLevelCreateList = createList;
	}

	public Map<String, List<String>> getNameDescriptionByLanguage() {
		return nameDescriptionByLanguage;
	}

	public void setNameDescriptionByLanguage(Map<String, List<String>> nameDescriptionByLanguage) {
		this.nameDescriptionByLanguage = nameDescriptionByLanguage;
	}

	public Map<TaxonomyLevel, Map<String, File>> getTaxonomyLevelToImageMap() {
		return taxonomyLevelToImage;
	}

	public void setTaxonomyLevelToImageMap(Map<TaxonomyLevel, Map<String, File>> taxonomyLevelToImage) {
		this.taxonomyLevelToImage = taxonomyLevelToImage;
	}
	
	public List<TaxonomyLevelType> getTaxonomyLevelTypeCreateList() {
		return taxonomyLevelTypeCreateList;
	}
	
	public void setTaxonomyLevelTypeCreateList(List<TaxonomyLevelType> taxonomyLevelTypeCreateList) {
		this.taxonomyLevelTypeCreateList = taxonomyLevelTypeCreateList;
	}
	
	public void setUpdateExistingTaxonomies(boolean updateExistingTaxonomies) {
		this.updateExistingTaxonomies = updateExistingTaxonomies;
	}
	
	public boolean isUpdateExistingTaxonomies() {
		return updateExistingTaxonomies;
	}
}
