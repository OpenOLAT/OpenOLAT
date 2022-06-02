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

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PlayMode;
import org.olat.course.nodes.practice.PracticeFilterRule;
import org.olat.course.nodes.practice.manager.SearchPracticeItemParametersHelper;
import org.olat.course.nodes.practice.ui.PracticeEditController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 12 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchPracticeItemParameters {
	
	private List<PracticeFilterRule> rules;
	private List<TaxonomyLevel> descendantsTaxonomyLevels;
	private List<String> descendantsTaxonomicPathKeys;
	private TaxonomyLevel exactTaxonomyLevel;
	private List<String> exactTaxonomicPathKeys;
	
	private boolean includeWithoutTaxonomyLevel;
	
	private PlayMode playMode;
	private RepositoryEntryRef courseEntry;
	private String subIdent;
	private IdentityRef identity;
	
	public static SearchPracticeItemParameters valueOf(IdentityRef identity, RepositoryEntry entry, PracticeCourseNode courseNode) {
		SearchPracticeItemParameters searchParams = new SearchPracticeItemParameters();
		List<PracticeFilterRule> rules = courseNode.getModuleConfiguration()
				.getList(PracticeEditController.CONFIG_KEY_FILTER_RULES, PracticeFilterRule.class);
		searchParams.setRules(rules);
		
		List<Long> selectedLevels = courseNode.getModuleConfiguration()
				.getList(PracticeEditController.CONFIG_KEY_FILTER_TAXONOMY_LEVELS, Long.class);
		if(selectedLevels != null && !selectedLevels.isEmpty()) {
			List<TaxonomyLevel> levels = CoreSpringFactory.getImpl(TaxonomyService.class)
					.getTaxonomyLevelsByKeys(selectedLevels);
			searchParams.setDescendantsLevels(levels);
		}
		
		boolean includeWOLevels = courseNode.getModuleConfiguration()
				.getBooleanSafe(PracticeEditController.CONFIG_KEY_FILTER_INCLUDE_WO_TAXONOMY_LEVELS, false);
		searchParams.setIncludeWithoutTaxonomyLevel(includeWOLevels);
		
		searchParams.setIdentity(identity);
		searchParams.setCourseEntry(entry);
		searchParams.setSubIdent(courseNode.getIdent());
		return searchParams;
	}

	public PlayMode getPlayMode() {
		return playMode;
	}

	public void setPlayMode(PlayMode playMode) {
		this.playMode = playMode;
	}

	public List<PracticeFilterRule> getRules() {
		return rules;
	}

	public void setRules(List<PracticeFilterRule> rules) {
		this.rules = rules;
	}
	
	public List<TaxonomyLevel> getDescendantsLevels() {
		return descendantsTaxonomyLevels;
	}
	
	public List<String> getDescendantsTaxonomicPathKeys() {
		return descendantsTaxonomicPathKeys;
	}

	public void setDescendantsLevels(List<TaxonomyLevel> descendantsLevels) {
		this.descendantsTaxonomyLevels = descendantsLevels;
		
		if(descendantsLevels == null || descendantsLevels.isEmpty()) {
			descendantsTaxonomicPathKeys = null;
		} else {
			descendantsTaxonomicPathKeys = new ArrayList<>(descendantsLevels.size() * 2);
			for(TaxonomyLevel descendantsLevel:descendantsLevels) {
				List<String> pathKeys = SearchPracticeItemParametersHelper
						.buildKeyOfTaxonomicPath(descendantsLevel);
				descendantsTaxonomicPathKeys.addAll(pathKeys);
			}
		}
	}

	public boolean isIncludeWithoutTaxonomyLevel() {
		return includeWithoutTaxonomyLevel;
	}

	public void setIncludeWithoutTaxonomyLevel(boolean includeWithoutTaxonomyLevel) {
		this.includeWithoutTaxonomyLevel = includeWithoutTaxonomyLevel;
	}

	public TaxonomyLevel getExactTaxonomyLevel() {
		return exactTaxonomyLevel;
	}

	public void setExactTaxonomyLevel(TaxonomyLevel exactTaxonomyLevel) {
		this.exactTaxonomyLevel = exactTaxonomyLevel;
		if(exactTaxonomyLevel == null) {
			exactTaxonomicPathKeys = null;
		} else {
			exactTaxonomicPathKeys = SearchPracticeItemParametersHelper
					.buildKeyOfTaxonomicPath(exactTaxonomyLevel);
		}
	}

	public List<String> getExactTaxonomicPathKeys() {
		return exactTaxonomicPathKeys;
	}

	public RepositoryEntryRef getCourseEntry() {
		return courseEntry;
	}

	public void setCourseEntry(RepositoryEntryRef courseEntry) {
		this.courseEntry = courseEntry;
	}

	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	public IdentityRef getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityRef identity) {
		this.identity = identity;
	}
}
