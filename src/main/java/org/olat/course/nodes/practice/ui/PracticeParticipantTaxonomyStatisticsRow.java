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
package org.olat.course.nodes.practice.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.course.nodes.practice.manager.SearchPracticeItemHelper;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 12 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeParticipantTaxonomyStatisticsRow {
	
	private final boolean withoutTaxonomy;
	private final Levels levels;
	private final String taxonomyLevelName;
	private final List<String> taxonomyPath;
	private final TaxonomyLevel taxonomyLevel;
	
	private List<TaxonomyLevel> aggregatedLevels;
	private final List<PracticeItem> cachedItems = new ArrayList<>();
	
	private final List<String> keys;
	
	private FormLink levelsLink;
	
	public PracticeParticipantTaxonomyStatisticsRow(String label, int numOfLevels) {
		keys = List.of();
		withoutTaxonomy = true;
		this.taxonomyLevelName = label;
		levels = new Levels(numOfLevels);
		taxonomyPath = null;
		taxonomyLevel = null;
	}
	
	public PracticeParticipantTaxonomyStatisticsRow(List<String> keys, TaxonomyLevel taxonomyLevel, int numOfLevels) {
		this.keys = keys;
		withoutTaxonomy = false;
		this.taxonomyLevel = taxonomyLevel;
		taxonomyLevelName = taxonomyLevel.getDisplayName();
		levels = new Levels(numOfLevels);
		taxonomyPath = SearchPracticeItemHelper
				.cleanTaxonomicParentLine(taxonomyLevelName, taxonomyLevel.getMaterializedPathIdentifiers());
	}
	
	public List<String> getKeys() {
		return keys;
	}
	
	public boolean withoutTaxonomy() {
		return withoutTaxonomy;
	}
	
	public boolean isEmpty() {
		return levels == null || levels.getTotal() <= 0;
	}

	public Levels getLevels() {
		return levels;
	}
	
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	public String getTaxonomyLevelName() {
		return taxonomyLevelName;
	}

	public List<String> getTaxonomyPath() {
		return taxonomyPath;
	}

	public List<TaxonomyLevel> getAggregatedLevels() {
		return aggregatedLevels;
	}

	public void appendRow(PracticeParticipantTaxonomyStatisticsRow row) {
		if(aggregatedLevels == null) {
			aggregatedLevels = new ArrayList<>();
		}
		aggregatedLevels.add(row.getTaxonomyLevel());
		levels.add(row.getLevels());
	}
	
	public FormLink getLevelsLink() {
		return levelsLink;
	}

	public void setLevelsLink(FormLink levelsLink) {
		this.levelsLink = levelsLink;
	}

	/**
	 * Especially for items without taxonomy level has this definition
	 * is somewhat complicated.
	 */
	public List<PracticeItem> getCachedItems() {
		return cachedItems;
	}

	public void cacheItem(PracticeItem item) {
		cachedItems.add(item);
	}
}
