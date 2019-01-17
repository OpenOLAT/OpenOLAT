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
package org.olat.modules.quality.analysis.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.IdentityShort;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.forms.ui.EvaluationFormFormatter;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class GroupByNameCache {
	
	private Map<String, String> groupNamesTopicIdentity;
	private Map<String, String> groupNamesTopicOrganisation;
	private Map<String, String> groupNamesTopicCurriculum;
	private Map<String, String> groupNamesTopicCurriculumElement;
	private Map<String, String> groupNamesTopicRepositoryEntry;
	private Map<String, String> groupNamesContextExecutorOrganisation;
	private Map<String, String> groupNamesContextCurriculum;
	private Map<String, String> groupNamesContextCurriculumElement;
	private Map<String, String> groupNamesContextCurriculumOrganisation;
	private Map<String, String> groupNamesContextTaxonomyLevel;
	private Map<String, String> groupNamesDataCollection;
	
	private Map<GroupBy, Map<String, String>> cache = new HashMap<>();

	private final AnalysisSearchParameter searchParams;
	private final Locale locale;

	@Autowired
	private QualityAnalysisService analysisService;
	
	GroupByNameCache(Locale locale) {
		this.locale = locale;
		searchParams = new AnalysisSearchParameter();
		CoreSpringFactory.autowireObject(this);
	}
	
	void init(RepositoryEntryRef formEntryRef) {
		if (changed(formEntryRef)) {
			searchParams.setFormEntryRef(formEntryRef);
			cache.clear();
		}
	}

	private boolean changed(RepositoryEntryRef formEntryRef) {
		return searchParams.getFormEntryRef() != null
				&& !formEntryRef.getKey().equals(searchParams.getFormEntryRef().getKey());
	}
	
	String getName(GroupBy groupBy, String key) {
		Map<String, String> names = cache.get(groupBy);
		if (names == null) {
			names = load(groupBy);
			cache.put(groupBy, names);
		}
		return names.get(key);
	}
	
	private Map<String, String> load(GroupBy groupBy) {
		switch (groupBy) {
		case TOPIC_IDENTITY:
			return loadTopicIdentityGroupNames();
		case TOPIC_ORGANISATION:
			return loadTopicOrganisationGroupNames();
		case TOPIC_CURRICULUM:
			return loadTopicCurriculumGroupNames();
		case TOPIC_CURRICULUM_ELEMENT:
			return loadTopicCurriculumElementGroupNames();
		case TOPIC_REPOSITORY:
			return loadTopicRepositoryEntryGroupNames();
		case CONTEXT_ORGANISATION:
			return loadContextExecutorOrganisationGroupNames();
		case CONTEXT_CURRICULUM:
			return loadContextCurriculumGroupNames();
		case CONTEXT_CURRICULUM_ELEMENT:
			return loadContextCurriculumElementGroupNames();
		case CONTEXT_CURRICULUM_ORGANISATION:
			return loadContextCurriculumOrganisationGroupNames();
		case CONTEXT_TAXONOMY_LEVEL:
			return loadContextTaxonomyLevelGroupNames();
		case DATA_COLLECTION:
			return loadDataCollectionGroupNames();
		default:
			return Collections.emptyMap();
		}
	}

	private Map<String, String> loadTopicIdentityGroupNames() {
		groupNamesTopicIdentity = new HashMap<>();
		List<IdentityShort> identities = analysisService.loadTopicIdentity(searchParams);
		for (IdentityShort identity : identities) {
			String key = identity.getKey().toString();
			String value = identity.getLastName() + " " + identity.getFirstName();
			groupNamesTopicIdentity.put(key, value);
		}
		return groupNamesTopicIdentity;
	}

	private Map<String, String> loadTopicOrganisationGroupNames() {
		groupNamesTopicOrganisation = new HashMap<>();
		List<Organisation> organisations = analysisService.loadTopicOrganisations(searchParams, false);
		for (Organisation organisation : organisations) {
			String key = organisation.getKey().toString();
			String value = organisation.getDisplayName();
			groupNamesTopicOrganisation.put(key, value);
		}
		return groupNamesTopicOrganisation;
	}

	private Map<String, String> loadTopicCurriculumGroupNames() {
		groupNamesTopicCurriculum = new HashMap<>();
		List<Curriculum> curriculums = analysisService.loadTopicCurriculums(searchParams);
		for (Curriculum curriculum : curriculums) {
			String key = curriculum.getKey().toString();
			String value = curriculum.getDisplayName();
			groupNamesTopicCurriculum.put(key, value);
		}
		return groupNamesTopicCurriculum;
	}

	private Map<String, String> loadTopicCurriculumElementGroupNames() {
		groupNamesTopicCurriculumElement = new HashMap<>();
		List<CurriculumElement> curriculumElements = analysisService.loadTopicCurriculumElements(searchParams);
		for (CurriculumElement curriculumElement : curriculumElements) {
			String key = curriculumElement.getKey().toString();
			String value = curriculumElement.getDisplayName();
			groupNamesTopicCurriculumElement.put(key, value);
		}
		return groupNamesTopicCurriculumElement;
	}

	private Map<String, String> loadTopicRepositoryEntryGroupNames() {
		groupNamesTopicRepositoryEntry = new HashMap<>();
		List<RepositoryEntry> entries = analysisService.loadTopicRepositoryEntries(searchParams);
		for (RepositoryEntry entry : entries) {
			String key = entry.getKey().toString();
			String value = entry.getDisplayname();
			groupNamesTopicRepositoryEntry.put(key, value);
		}
		return groupNamesTopicRepositoryEntry;
	}

	private Map<String, String> loadContextExecutorOrganisationGroupNames() {
		groupNamesContextExecutorOrganisation = new HashMap<>();
		List<Organisation> elements = analysisService.loadContextExecutorOrganisations(searchParams, false);
		for (Organisation element : elements) {
			String key = element.getKey().toString();
			String value = element.getDisplayName();
			groupNamesContextExecutorOrganisation.put(key, value);
		}
		return groupNamesContextExecutorOrganisation;
	}

	private Map<String, String> loadContextCurriculumGroupNames() {
		groupNamesContextCurriculum = new HashMap<>();
		List<Curriculum> elements = analysisService.loadContextCurriculums(searchParams);
		for (Curriculum element : elements) {
			String key = element.getKey().toString();
			String value = element.getDisplayName();
			groupNamesContextCurriculum.put(key, value);
		}
		return groupNamesContextCurriculum;
	}

	private Map<String, String> loadContextCurriculumElementGroupNames() {
		groupNamesContextCurriculumElement = new HashMap<>();
		List<CurriculumElement> elements = analysisService.loadContextCurriculumElements(searchParams, false);
		for (CurriculumElement element : elements) {
			String key = element.getKey().toString();
			String value = element.getDisplayName();
			groupNamesContextCurriculumElement.put(key, value);
		}
		return groupNamesContextCurriculumElement;
	}
	
	private Map<String, String> loadContextCurriculumOrganisationGroupNames() {
		groupNamesContextCurriculumOrganisation = new HashMap<>();
		List<Organisation> elements = analysisService.loadContextCurriculumOrganisations(searchParams, false);
		for (Organisation element : elements) {
			String key = element.getKey().toString();
			String value = element.getDisplayName();
			groupNamesContextCurriculumOrganisation.put(key, value);
		}
		return groupNamesContextCurriculumOrganisation;
	}

	private Map<String, String> loadContextTaxonomyLevelGroupNames() {
		groupNamesContextTaxonomyLevel = new HashMap<>();
		List<TaxonomyLevel> elements = analysisService.loadContextTaxonomyLevels(searchParams, false);
		for (TaxonomyLevel element : elements) {
			String key = element.getKey().toString();
			String value = element.getDisplayName();
			groupNamesContextTaxonomyLevel.put(key, value);
		}
		return groupNamesContextTaxonomyLevel;
	}
	
	private Map<String, String> loadDataCollectionGroupNames() {
		groupNamesDataCollection = new HashMap<>();
		List<QualityDataCollection> dataCollections = analysisService.loadDataCollections(searchParams);
		for (QualityDataCollection dataCollection : dataCollections) {
			String key = dataCollection.getKey().toString();
			String period = EvaluationFormFormatter.period(dataCollection.getStart(), dataCollection.getDeadline(), locale);
			StringBuilder sb = new StringBuilder();
			sb.append(StringHelper.escapeHtml(dataCollection.getTitle()));
			if (period != null) {
				sb.append("<small> (").append(StringHelper.escapeHtml(period)).append(")</small>");
			}
			String value = sb.toString();
			groupNamesDataCollection.put(key, value);
		}
		return groupNamesDataCollection;
	}

}
