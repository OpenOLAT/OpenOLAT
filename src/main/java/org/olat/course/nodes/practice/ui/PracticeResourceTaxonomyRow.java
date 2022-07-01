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
 * Initial date: 6 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeResourceTaxonomyRow {
	
	private final String taxonomyLevel;
	private final List<String> taxonomyPath;
	private int numOfQuestions = 0;
	private final boolean withoutTaxonomy;
	
	public PracticeResourceTaxonomyRow(TaxonomyLevel level, String displayName) {
		withoutTaxonomy = false;
		taxonomyLevel = displayName;
		taxonomyPath = SearchPracticeItemHelper.cleanTaxonomicParentLine(taxonomyLevel, level.getMaterializedPathIdentifiers());
	}
	
	public PracticeResourceTaxonomyRow(String label, int numOfQuestions) {
		withoutTaxonomy = true;
		taxonomyLevel = label;
		taxonomyPath = List.of();
		this.numOfQuestions = numOfQuestions;
	}
	
	public boolean withoutTaxonomy() {
		return withoutTaxonomy;
	}

	public String getTaxonomyLevel() {
		return taxonomyLevel;
	}
	
	public List<String> getTaxonomyPath() {
		return taxonomyPath;
	}
	
	public boolean isEmpty() {
		return numOfQuestions <= 0;
	}

	public int getNumOfQuestions() {
		return numOfQuestions;
	}
	
	public void incrementNumOfQuestions() {
		++numOfQuestions;
	}
}
