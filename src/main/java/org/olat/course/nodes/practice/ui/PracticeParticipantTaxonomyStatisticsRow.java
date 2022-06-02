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

import java.util.List;

import org.olat.course.nodes.practice.manager.SearchPracticeItemHelper;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 12 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeParticipantTaxonomyStatisticsRow {
	
	private final Levels levels;
	private final String taxonomyLevelName;
	private final List<String> taxonomyPath;
	private final String taxonomicPathKey;
	private final TaxonomyLevel taxonomyLevel;
	
	public PracticeParticipantTaxonomyStatisticsRow(String label, int numOfLevels) {
		this.taxonomyLevelName = label;
		levels = new Levels(numOfLevels);
		taxonomyPath = null;
		taxonomyLevel = null;
		taxonomicPathKey = null;
	}
	
	public PracticeParticipantTaxonomyStatisticsRow(TaxonomyLevel taxonomyLevel, int numOfLevels) {
		this.taxonomyLevel = taxonomyLevel;
		taxonomyLevelName = taxonomyLevel.getDisplayName();
		levels = new Levels(numOfLevels);
		taxonomyPath = SearchPracticeItemHelper.cleanTaxonomicParentLine(taxonomyLevelName, taxonomyLevel.getMaterializedPathIdentifiers());
		taxonomicPathKey = SearchPracticeItemHelper.buildKeyOfTaxonomicPath(taxonomyLevelName, taxonomyPath);
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

	public String getTaxonomicPathKey() {
		return taxonomicPathKey;
	}
	
}
