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
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.forms.ui.EvaluationFormFormatter;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.GroupByKey;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.ui.QualityMainController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
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
	
	private Map<GroupBy, Map<String, Name>> cache = new HashMap<>();

	private final AnalysisSearchParameter searchParams;
	private final Locale locale;
	private Translator translator;

	@Autowired
	private QualityAnalysisService analysisService;
	
	GroupByNameCache(Locale locale) {
		this.locale = locale;
		searchParams = new AnalysisSearchParameter();
		CoreSpringFactory.autowireObject(this);
	}
	
	void init(RepositoryEntryRef formEntryRef, List<? extends OrganisationRef> dataCollectionOrganisationRefs) {
		if (changed(formEntryRef)) {
			searchParams.setFormEntryRef(formEntryRef);
			searchParams.setDataCollectionOrganisationRefs(dataCollectionOrganisationRefs);
			cache.clear();
		}
	}

	private boolean changed(RepositoryEntryRef formEntryRef) {
		return searchParams.getFormEntryRef() != null
				&& !formEntryRef.getKey().equals(searchParams.getFormEntryRef().getKey());
	}
	
	Name getName(GroupByKey groupByKey) {
		if (groupByKey == null) return null;
		
		GroupBy groupBy = groupByKey.getGroupBy();
		Map<String, Name> names = cache.get(groupBy);
		if (names == null) {
			names = load(groupBy);
			cache.put(groupBy, names);
		}
		Name name = names.get(groupByKey.getKey());
		if (name == null ) {
			name = new UnformatedName(null);
		}
		return name;
	}
	
	private Map<String, Name> load(GroupBy groupBy) {
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
		case CONTEXT_LOCATION:
			return loadContextLocationGroupNames();
		case DATA_COLLECTION:
			return loadDataCollectionGroupNames();
		default:
			return Collections.emptyMap();
		}
	}

	private Map<String, Name> loadTopicIdentityGroupNames() {
		Map<String, Name> keyToName = new HashMap<>();
		List<IdentityShort> identities = analysisService.loadTopicIdentity(searchParams);
		for (IdentityShort identity : identities) {
			String key = identity.getKey().toString();
			String value = identity.getStatus() >= Identity.STATUS_DELETED
					? getTranslator().translate("data.collection.topic.identity.unknown")
					: identity.getLastName() + " " + identity.getFirstName();
			keyToName.put(key, new UnformatedName(value));
		}
		return keyToName;
	}
	
	private Translator getTranslator() {
		if (translator == null) {
			translator = Util.createPackageTranslator(QualityMainController.class, locale);
		}
		return translator;
	}

	private Map<String, Name> loadTopicOrganisationGroupNames() {
		Map<String, Name> keyToName = new HashMap<>();
		List<Organisation> organisations = analysisService.loadTopicOrganisations(searchParams, false);
		for (Organisation organisation : organisations) {
			String key = organisation.getKey().toString();
			String value = organisation.getDisplayName();
			keyToName.put(key, new UnformatedName(value));
		}
		return keyToName;
	}

	private Map<String, Name> loadTopicCurriculumGroupNames() {
		Map<String, Name> keyToName = new HashMap<>();
		List<Curriculum> curriculums = analysisService.loadTopicCurriculums(searchParams);
		for (Curriculum curriculum : curriculums) {
			String key = curriculum.getKey().toString();
			String value = curriculum.getDisplayName();
			keyToName.put(key, new UnformatedName(value));
		}
		return keyToName;
	}

	private Map<String, Name> loadTopicCurriculumElementGroupNames() {
		Map<String, Name> keyToName = new HashMap<>();
		List<CurriculumElement> curriculumElements = analysisService.loadTopicCurriculumElements(searchParams);
		for (CurriculumElement curriculumElement : curriculumElements) {
			String key = curriculumElement.getKey().toString();
			String value = curriculumElement.getDisplayName();
			keyToName.put(key, new UnformatedName(value));
		}
		return keyToName;
	}

	private Map<String, Name> loadTopicRepositoryEntryGroupNames() {
		Map<String, Name> keyToName = new HashMap<>();
		List<RepositoryEntry> entries = analysisService.loadTopicRepositoryEntries(searchParams);
		for (RepositoryEntry entry : entries) {
			String key = entry.getKey().toString();
			String value = entry.getDisplayname();
			keyToName.put(key, new UnformatedName(value));
		}
		return keyToName;
	}

	private Map<String, Name> loadContextExecutorOrganisationGroupNames() {
		Map<String, Name> keyToName = new HashMap<>();
		List<Organisation> elements = analysisService.loadContextExecutorOrganisations(searchParams, false);
		for (Organisation element : elements) {
			String key = element.getKey().toString();
			String value = element.getDisplayName();
			keyToName.put(key, new UnformatedName(value));
		}
		return keyToName;
	}

	private Map<String, Name> loadContextCurriculumGroupNames() {
		Map<String, Name> keyToName = new HashMap<>();
		List<Curriculum> elements = analysisService.loadContextCurriculums(searchParams);
		for (Curriculum element : elements) {
			String key = element.getKey().toString();
			String value = element.getDisplayName();
			keyToName.put(key, new UnformatedName(value));
		}
		return keyToName;
	}

	private Map<String, Name> loadContextCurriculumElementGroupNames() {
		Map<String, Name> keyToName = new HashMap<>();
		List<CurriculumElement> elements = analysisService.loadContextCurriculumElements(searchParams, false);
		for (CurriculumElement element : elements) {
			String key = element.getKey().toString();
			String value = element.getDisplayName();
			keyToName.put(key, new UnformatedName(value));
		}
		return keyToName;
	}
	
	private Map<String, Name> loadContextCurriculumOrganisationGroupNames() {
		Map<String, Name> keyToName = new HashMap<>();
		List<Organisation> elements = analysisService.loadContextCurriculumOrganisations(searchParams, false);
		for (Organisation element : elements) {
			String key = element.getKey().toString();
			String value = element.getDisplayName();
			keyToName.put(key, new UnformatedName(value));
		}
		return keyToName;
	}

	private Map<String, Name> loadContextTaxonomyLevelGroupNames() {
		List<TaxonomyLevel> levels = analysisService.loadContextTaxonomyLevels(searchParams, false);
		Translator taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
		Map<String, Name> keyToName = new HashMap<>(levels.size());
		for (TaxonomyLevel level : levels) {
			String key = level.getKey().toString();
			String value = TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, level);
			keyToName.put(key, new UnformatedName(value));
		}
		return keyToName;
	}

	private Map<String, Name> loadContextLocationGroupNames() {
		Map<String, Name> keyToName = new HashMap<>();
		List<String> contextLocations = analysisService.loadContextLocations(searchParams);
		for (String location: contextLocations) {
			String key = location;
			String value = location;
			keyToName.put(key, new UnformatedName(value));
		}
		return keyToName;
	}
	
	private Map<String, Name> loadDataCollectionGroupNames() {
		Map<String, Name> keyToName = new HashMap<>();
		List<QualityDataCollection> dataCollections = analysisService.loadDataCollections(searchParams);
		for (QualityDataCollection dataCollection : dataCollections) {
			String key = dataCollection.getKey().toString();
			String plainName = getPlainDataCollectionName(dataCollection);
			String formatedName = getFormatedDataCollectionName(dataCollection);
			keyToName.put(key, new FormatedName(plainName, formatedName));
		}
		return keyToName;
	}
	
	private String getPlainDataCollectionName(QualityDataCollection dataCollection) {
		String period = EvaluationFormFormatter.period(dataCollection.getStart(), dataCollection.getDeadline(), locale);
		StringBuilder sb = new StringBuilder();
		sb.append(dataCollection.getTitle());
		if (period != null) {
			sb.append(" (").append(StringHelper.escapeHtml(period)).append(")");
		}
		return sb.toString();
	}
	
	private String getFormatedDataCollectionName(QualityDataCollection dataCollection) {
		String period = EvaluationFormFormatter.period(dataCollection.getStart(), dataCollection.getDeadline(), locale);
		StringBuilder sb = new StringBuilder();
		sb.append(StringHelper.escapeHtml(dataCollection.getTitle()));
		if (period != null) {
			sb.append("<small> (").append(StringHelper.escapeHtml(period)).append(")</small>");
		}
		return sb.toString();
	}

	static interface Name {
		
		String getPlainName();
		
		String getFormatedName();
	}
	
	static class UnformatedName implements Name {
		
		private String plainName;

		private UnformatedName(String plainName) {
			this.plainName = plainName;
		}

		@Override
		public String getPlainName() {
			return plainName;
		}

		@Override
		public String getFormatedName() {
			return plainName;
		}
		
	}
	
	static class FormatedName implements Name {
		
		private String plainName;
		private String formatedName;
		
		private FormatedName(String plainName, String formatedName) {
			this.plainName = plainName;
			this.formatedName = formatedName;
		}

		@Override
		public String getPlainName() {
			return plainName;
		}

		@Override
		public String getFormatedName() {
			return formatedName;
		}
		
	}

}
