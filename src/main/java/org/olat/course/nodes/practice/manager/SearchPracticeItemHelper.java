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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.course.nodes.practice.PracticeFilterRule;
import org.olat.course.nodes.practice.PracticeFilterRule.Type;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 13 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchPracticeItemHelper {
	
	private SearchPracticeItemHelper() {
		//
	}
	
	public static boolean autoAnswer(QuestionItem item) {
		String type = item.getItemType();
		return !QTI21QuestionType.drawing.name().equals(type)
				&& !QTI21QuestionType.essay.name().equals(type)
				&& !QTI21QuestionType.upload.name().equals(type);
	}

	public static boolean accept(QuestionItem item, SearchPracticeItemParameters searchParams) {
		String taxonomicPathKey = buildKeyOfTaxonomicPath(item.getTaxonomyLevelName(), item.getTaxonomicPath());
		if(searchParams.getExactTaxonomyLevel() != null
				&& (taxonomicPathKey == null || !searchParams.getExactTaxonomicPathKeys().contains(taxonomicPathKey))) {
			return false;
		}
		
		if(!accept(taxonomicPathKey, searchParams.getDescendantsTaxonomicPathKeys(), searchParams.isIncludeWithoutTaxonomyLevel())) {
			return false;
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
	
	public static boolean accept(PracticeItem item, List<String> levelsPathKeys, boolean includeWithoutTaxonomy) {
		String taxonomicPathKey = buildKeyOfTaxonomicPath(item.getTaxonomyLevelName(), item.getTaxonomicPath());
		return accept(taxonomicPathKey, levelsPathKeys, includeWithoutTaxonomy);
	}
	
	public static boolean accept(String taxonomicPathKey, List<String> levelsPathKeys, boolean includeWithoutTaxonomy) {
		if(levelsPathKeys != null && !levelsPathKeys.isEmpty()) {
			if(taxonomicPathKey == null && !includeWithoutTaxonomy) {
				return false;
			}
			if(taxonomicPathKey != null && !levelsPathKeys.contains(taxonomicPathKey)) {
				return false;
			}
		} else if (includeWithoutTaxonomy && StringHelper.containsNonWhitespace(taxonomicPathKey)) {
			return false;
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
	
	
	public static List<String> cleanTaxonomicParentLine(String displayName, String taxonomicPath) {
		List<String> path;
		if(StringHelper.containsNonWhitespace(taxonomicPath)) {
			String[] pathArray = taxonomicPath.split("[/]");
			path = new ArrayList<>(pathArray.length);
			for(String segment:pathArray) {
				if(StringHelper.containsNonWhitespace(segment)) {
					path.add(segment);
				}
			}
			
			if(!path.isEmpty() && path.get(path.size() - 1).equals(displayName)) {
				path = path.subList(0, path.size() -1);
			}
		} else {
			path = List.of();
		}
		return path;
	}
	
	public static String buildKeyOfTaxonomicPath(String displayName, String taxonomicPath) {
		List<String> parentLine = cleanTaxonomicParentLine( displayName, taxonomicPath);
		return buildKeyOfTaxonomicPath(displayName, parentLine);
	}
	
	public static String buildKeyOfTaxonomicPath(String displayName, List<String> taxonomicPath) {
		StringBuilder sb = new StringBuilder();
		if(taxonomicPath != null && !taxonomicPath.isEmpty()) {
			for(String segment:taxonomicPath) {
				if(sb.length() == 0) {
					sb.append("/");
				}
				sb.append(segment).append("/");
			}
		}
		if(StringHelper.containsNonWhitespace(displayName)) {
			if(sb.length() == 0) {
				sb.append("/");
			}
			sb.append(displayName).append("/");
		}
		return sb.length() == 0 ? null : sb.toString();
	}
	
	public static List<String> buildKeyOfTaxonomicPath(TaxonomyLevel level) {
		String identifiers = level.getMaterializedPathIdentifiers();
		return List.of(identifiers);
	}	
}
