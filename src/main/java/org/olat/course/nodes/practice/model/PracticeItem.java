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
package org.olat.course.nodes.practice.model;

import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;

/**
 * Wrapper to hold assessment item ref from a test or question item of a pool.
 * 
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeItem {

	private final String identifier;
	private final String displayName;
	
	private final AssessmentItemRef ref;
	private final QuestionItem item;
	
	private final RepositoryEntry entry;
	
	private final String taxonomyLevelName;
	private final String taxonomicPath;
	private final TaxonomyLevel taxonomyLevel;
	
	public PracticeItem(QuestionItem item) {
		this(item.getIdentifier(), item.getTitle(), null, item, null);
	}
	
	/**
	 * 
	 * 
	 * @param identifier The identifier in the assessment test
	 * @param ref The assessment item reference
	 * @param item The metadata of the item
	 * @param entry the test
	 */
	public PracticeItem(String identifier, String displayName, AssessmentItemRef ref, QuestionItem item, RepositoryEntry entry) {
		this.ref = ref;
		this.item = item;
		this.entry = entry;
		this.identifier = identifier;
		this.displayName = displayName;
		taxonomyLevelName = item == null ? null : item.getTaxonomyLevelName();
		taxonomicPath = item == null ? null : item.getTaxonomicPath();
		taxonomyLevel = item == null ? null : item.getTaxonomyLevel();
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public QuestionItem getItem() {
		return item;
	}

	public AssessmentItemRef getItemRef() {
		return ref;
	}

	public RepositoryEntry getRepositoryEntry() {
		return entry;
	}

	public String getTaxonomyLevelName() {
		return taxonomyLevelName;
	}

	public String getTaxonomicPath() {
		return taxonomicPath;
	}
	
	/**
	 * This is the taxonomy level of OpenOlat, the item can have other metadata and
	 * level name and path without having the taxonomy level.
	 * 
	 * @return A taxonomy level if found on the database.
	 */
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}
}
