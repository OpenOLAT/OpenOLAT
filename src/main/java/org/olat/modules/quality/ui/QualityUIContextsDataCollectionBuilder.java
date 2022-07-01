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
package org.olat.modules.quality.ui;

import static org.olat.modules.quality.ui.QualityUIFactory.formatTopic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityContextToCurriculumElement;
import org.olat.modules.quality.QualityContextToTaxonomyLevel;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityUIContextsDataCollectionBuilder extends QualityUIContextsBuilder {
	
	private static final String DELIMITER = ", ";
	
	private final QualityDataCollection dataCollection;
	private final Translator translator;
	
	@Autowired
	private QualityService qualityService;
	@Autowired
	private CurriculumService curriculumService;
	
	QualityUIContextsDataCollectionBuilder(QualityDataCollection dataCollection, Locale locale) {
		this.dataCollection = dataCollection;
		translator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale,
				Util.createPackageTranslator(QualityMainController.class, locale));
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public UIContexts build() {
		List<QualityContext> contexts = qualityService.loadContextByDataCollection(dataCollection);
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		searchParams.setDataCollectionRef(dataCollection);
		List<QualityDataCollectionView> dataCollectionViews = qualityService.loadDataCollections(translator, searchParams, 0, -1);
		QualityDataCollectionView dataCollectionView = !dataCollectionViews.isEmpty()? dataCollectionViews.get(0): null;
		
		UIContext uiContext = new UIContext();
		if (dataCollectionView != null) {
			if (attributes.contains(Attribute.TOPIC)) {
				String key = translator.translate("executor.participation.topic.title");
				String value = formatTopic(dataCollectionView, translator.getLocale());
				KeyValue keyValue = new KeyValue(key, value);
				uiContext.add(keyValue);
			}
			if (attributes.contains(Attribute.PREVIOUS_TOPIC)) {
				String value = dataCollectionView.getPreviousTitle();
				if (StringHelper.containsNonWhitespace(value)) {
					String key = translator.translate("executor.participation.figures.previous.title");
					KeyValue keyValue = new KeyValue(key, value);
					uiContext.add(keyValue);
				}
			}
		}
		if (!contexts.isEmpty()) {
			if (attributes.contains(Attribute.ROLE)) {
				String value = getRoles(contexts);
				if (StringHelper.containsNonWhitespace(value)) {
					String key = translator.translate("executor.participation.rating");
					KeyValue keyValue = new KeyValue(key, value);
					uiContext.add(keyValue);
				}
			}
			if (attributes.contains(Attribute.CURRICULUM_ELEMENTS)) {
				Collection<KeyValue> keyValues = getCurriculumElements(contexts);
				uiContext.addAll(keyValues);
			}
			if (attributes.contains(Attribute.COURSE)) {
				String value = getAudienceCourses(contexts);
				if (StringHelper.containsNonWhitespace(value)) {
					String key = translator.translate("executor.participation.repository");
					KeyValue keyValue = new KeyValue(key, value);
					uiContext.add(keyValue);
				}
			}
			if (attributes.contains(Attribute.TAXONOMY_LEVELS)) {
				Collection<KeyValue> keyValues = getTaxonomyLevels(contexts);
				uiContext.addAll(keyValues);
			}
		}
		
		if (alwaysEvenKeyValues) {
			uiContext.add(new KeyValue("", ""));
		}

		UIContexts uiContexts = new UIContexts();
		uiContexts.add(uiContext);
		return uiContexts;
	}
	
	private String getRoles(List<QualityContext> contexts) {
		return contexts.stream()
				.map(QualityContext::getRole)
				.distinct()
				.map(this::translateRole)
				.filter(Objects::nonNull)
				.collect(Collectors.joining(DELIMITER));
	}
	
	private String translateRole(QualityContextRole role) {
		switch (role) {
		case owner: return translator.translate("participation.role.owner");
		case coach: return translator.translate("participation.role.coach");
		case participant: return translator.translate("participation.role.participant");
		default: return null;
		}
	}
	
	private String getAudienceCourses(List<QualityContext> contexts) {
		return contexts.stream()
				.map(QualityContext::getAudienceRepositoryEntry)
				.distinct()
				.filter(Objects::nonNull)
				.map(RepositoryEntry::getDisplayname)
				.collect(Collectors.joining(DELIMITER));
	}
	
	private List<KeyValue> getCurriculumElements(List<QualityContext> contexts) {
		// Get a list of all curriculum elements and its parents.
		List<CurriculumElement> elements = contexts.stream()
				.map(QualityContext::getContextToCurriculumElement)
				.flatMap(Set::stream)
				.map(QualityContextToCurriculumElement::getCurriculumElement)
				.distinct()
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		
		// reload to fetch the parents
		elements = curriculumService.getCurriculumElements(elements);
		List<CurriculumElement> elementsHierarchical = new ArrayList<>();
		for (CurriculumElement element: elements) {
			addParents(elementsHierarchical, element);
		}
		// reverse, so that the top most element is the first element
		List<CurriculumElement> invertedList = new ArrayList<>();
		for (int i = elementsHierarchical.size() - 1; i >= 0; i--) {
			invertedList.add(elementsHierarchical.get(i));
		}
		elementsHierarchical = invertedList;
		
		List<KeyValue> keyValues = new ArrayList<>(elementsHierarchical.size());
		for (CurriculumElement element : elementsHierarchical) {
			KeyValue keyValue = new KeyValue(getTypeName(element), getName(element));
			keyValues.add(keyValue);
		}
		return keyValues;
	}

	private void addParents(List<CurriculumElement> elementsHierarchical, CurriculumElement element) {
		elementsHierarchical.add(element);
		if (element.getParent() != null) {
			addParents(elementsHierarchical, element.getParent());
		}
	}
	
	private String getName(CurriculumElement element) {
		StringBuilder sb = new StringBuilder(element.getDisplayName());
		if (StringHelper.containsNonWhitespace(element.getIdentifier())) {
			sb.append(" (").append(element.getIdentifier()).append(")");
		}
		return sb.toString();
	}

	private String getTypeName(CurriculumElement element) {
		CurriculumElementType curriculumElementType = element.getType();
		return curriculumElementType != null
				? curriculumElementType.getDisplayName()
				: translator.translate("executor.participation.curriculum.element");
	}
	
	private Collection<KeyValue> getTaxonomyLevels(List<QualityContext> contexts) {
		// Group taxonomy levels by the name of the type and join the display names
		Map<String, String> typesNamesToTaxonomyLevels = contexts.stream()
				.map(QualityContext::getContextToTaxonomyLevel)
				.flatMap(Set::stream)
				.map(QualityContextToTaxonomyLevel::getTaxonomyLevel)
				.distinct()
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(this::getLevelName,
					Collectors.mapping(level -> TaxonomyUIFactory.translateDisplayName(translator, level), Collectors.joining(DELIMITER))));
		
		List<KeyValue> keyValues = new ArrayList<>(typesNamesToTaxonomyLevels.size());
		for (Map.Entry<String, String> entry : typesNamesToTaxonomyLevels.entrySet()) {
			KeyValue keyValue = new KeyValue(entry.getKey(), entry.getValue());
			keyValues.add(keyValue);
		}
		return keyValues;
	}
	
	private String getLevelName(TaxonomyLevel taxonomyLevel) {
		TaxonomyLevelType taxonomyLevelType = taxonomyLevel.getType();
		return taxonomyLevelType != null
				? taxonomyLevelType.getDisplayName()
				: translator.translate("executor.participation.taxonomy.level");
	}

}
