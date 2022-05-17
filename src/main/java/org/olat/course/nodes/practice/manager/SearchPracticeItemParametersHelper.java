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
package org.olat.course.nodes.practice.manager;

import java.util.List;

import org.olat.course.nodes.practice.PracticeFilterRule;
import org.olat.course.nodes.practice.PracticeFilterRule.Type;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 13 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchPracticeItemParametersHelper {
	
	private SearchPracticeItemParametersHelper() {
		//
	}

	public static boolean accept(QuestionItem item, SearchPracticeItemParameters searchParams) {
		TaxonomyLevel taxonomyLevel = item.getTaxonomyLevel();
		if(searchParams.getExactTaxonomyLevelKey() != null
				&& (taxonomyLevel == null || !taxonomyLevel.getKey().equals(searchParams.getExactTaxonomyLevelKey()))) {
			return false;
		}

		if(searchParams.getDescendantsLevels() != null && !searchParams.getDescendantsLevels().isEmpty()) {
			if(taxonomyLevel == null) {
				return false;
			}
			if(!searchParams.getDescendantsLevels().contains(taxonomyLevel)) {
				return false;
			}
		}

		List<PracticeFilterRule> rules = searchParams.getRules();
		if(rules != null && !rules.isEmpty()) {
			for(PracticeFilterRule rule:rules) {
				if(!accept(item, rule)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean accept(QuestionItem item, PracticeFilterRule rule) {
		if(rule.getType() == Type.language) {
			return rule.getValue().equalsIgnoreCase(item.getLanguage());
		}
		
		if(rule.getType() == Type.keyword) {
			String keywords = item.getKeywords();
			return keywords != null && keywords.toLowerCase().contains(rule.getValue().toLowerCase());
		}
		
		if(rule.getType() == Type.educationalContextLevel) {
			QEducationalContext context = item.getEducationalContext();
			return context != null && context.getKey().equals(Long.valueOf(rule.getValue()));
		}
		
		if(rule.getType() == Type.assessmentType) {
			String assessmentType = item.getAssessmentType();
			return assessmentType != null && assessmentType.equals(rule.getValue());
		}
		return true;
	}
}
