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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityContextToCurriculumElement;
import org.olat.modules.quality.QualityContextToTaxonomyLevel;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityUIContextsBuilder {
	
	public enum Attribute {TOPIC, PREVIOUS_TOPIC, ROLE, COURSE, CURRICULUM_ELEMENTS, TAXONOMY_LEVELS};
	
	private final QualityExecutorParticipation qualityParticipation;
	private final Translator translator;
	private List<Attribute> attributes = new ArrayList<>();
	private boolean alwaysEvenKeyValues;
	
	@Autowired
	private QualityService qualityService;
	
	public static QualityUIContextsBuilder builder(QualityExecutorParticipation participation, Locale locale) {
		return new QualityUIContextsBuilder(participation, locale);
	}
	
	public QualityUIContextsBuilder addAttribute(Attribute attribute) {
		attributes.add(attribute);
		return this;
	}
	
	public QualityUIContextsBuilder withAllAttributes() {
		attributes =  Arrays.stream(Attribute.values()).collect(Collectors.toList());
		return this;
	}
	
	public QualityUIContextsBuilder withAlwaysEvenKeyValues() {
		alwaysEvenKeyValues = true;
		return this;
	}
	
	private QualityUIContextsBuilder(QualityExecutorParticipation participation, Locale locale) {
		this.qualityParticipation = participation;
		translator = Util.createPackageTranslator(QualityMainController.class, locale);
		CoreSpringFactory.autowireObject(this);
	}

	public UIContexts build() {
		List<QualityContext> contexts = qualityService.loadContextByParticipation(qualityParticipation.getParticipationRef());
		UIContexts uiContexts = new UIContexts();
		for (QualityContext context: contexts) {
			UIContext uiContext = createUIContext(context);
			uiContexts.add(uiContext);
		}
		return uiContexts;
	}
	
	private UIContext createUIContext(QualityContext context) {
		UIContext uiContext = new UIContext();
		for (Attribute attribute : attributes) {
			List<KeyValue> keyValues = createKeyValues(attribute, context);
			uiContext.addAll(keyValues);
		}
		if (alwaysEvenKeyValues) {
			if (uiContext.getKeyValues().size() % 2 == 1) {
				// Add empty entry to have an even number of entries
				uiContext.add(new KeyValue("", ""));
			}
		}
		return uiContext;
	}

	private List<KeyValue> createKeyValues(Attribute attribute, QualityContext context) {
		switch (attribute) {
		case TOPIC:
			return toList(createTopicKeyValue());
		case PREVIOUS_TOPIC:
			return toList(createPreviousKeyValue());
		case ROLE:
			return toList(createRoleKeyValue(context));
		case COURSE:
			return toList(createRepositoryEntryValues(context));
		case CURRICULUM_ELEMENTS:
			return createCurriculumElementValues(context);
		case TAXONOMY_LEVELS:
			return createTaxonomyLevels(context);
		default:
			return Collections.emptyList();
		}
	}
	
	private List<KeyValue> toList(KeyValue keyValue) {
		if (keyValue == null) return Collections.emptyList();
		
		return Arrays.asList(keyValue);
	}

	private KeyValue createTopicKeyValue() {
		KeyValue keyValue = null;
		if (qualityParticipation.getTopic() != null) {
			String key = translator.translate("executor.participation.topic.title");
			String value = formatTopic(qualityParticipation);
			keyValue = new KeyValue(key, value);
		}
		return keyValue;
	}

	private KeyValue createPreviousKeyValue() {
		KeyValue keyValue = null;
		if (qualityParticipation.getPreviousTitle() != null) {
			String key = translator.translate("executor.participation.previous.title");
			String value = qualityParticipation.getPreviousTitle();
			keyValue = new KeyValue(key, value);
		}
		return keyValue;
	}

	private KeyValue createRoleKeyValue(QualityContext context) {
		KeyValue keyValue = null;
		if (context.getRole() != null && !context.getRole().equals(QualityContextRole.none)) {
			String key = translator.translate("executor.participation.rating");
			String value = translateRole(context.getRole());
			keyValue = new KeyValue(key, value);
		}
		return keyValue;
	}

	private String translateRole(QualityContextRole role) {
		switch (role) {
		case owner: return translator.translate("executor.participation.owner");
		case coach: return translator.translate("executor.participation.coach");
		case participant: return translator.translate("executor.participation.participant");
		default: return "";
		}
	}

	private KeyValue createRepositoryEntryValues(QualityContext context) {
		KeyValue keyValue = null;
		if (context.getAudienceRepositoryEntry() != null) {
			String key = translator.translate("executor.participation.repository");
			String value = context.getAudienceRepositoryEntry().getDisplayname();
			keyValue = new KeyValue(key, value);
		}
		return keyValue;
	}

	private List<KeyValue> createCurriculumElementValues(QualityContext context) {
		List<KeyValue> keyValues = new ArrayList<>();
		for (QualityContextToCurriculumElement contextToCurriculumelement: context.getContextToCurriculumElement()) {
			CurriculumElement curriculumElement = contextToCurriculumelement.getCurriculumElement();
			if (curriculumElement != null) {
				CurriculumElementType curriculumElementType = curriculumElement.getType();
				String key;
				if (curriculumElementType != null) {
					key = curriculumElementType.getDisplayName();
				} else {
					key = translator.translate("executor.participation.curriculum.element");
				}
				String value = curriculumElement.getDisplayName();
				KeyValue keyValue = new KeyValue(key, value);
				keyValues.add(keyValue);
			}
		}
		return keyValues;
	}

	private List<KeyValue> createTaxonomyLevels(QualityContext context) {
		List<KeyValue> keyValues = new ArrayList<>();
		for (QualityContextToTaxonomyLevel contextToTaxonomyLevel: context.getContextToTaxonomyLevel()) {
			TaxonomyLevel taxonomyLevel = contextToTaxonomyLevel.getTaxonomyLevel();
			if (taxonomyLevel != null) {
				TaxonomyLevelType taxonomyLevelType = taxonomyLevel.getType();
				String key;
				if (taxonomyLevelType != null) {
					key = taxonomyLevelType.getDisplayName();
				} else {
					key = translator.translate("executor.participation.taxonomy.level");
				}
				String value = taxonomyLevel.getDisplayName();
				KeyValue keyValue = new KeyValue(key, value);
				keyValues.add(keyValue);
			}
		}
		return keyValues;
	}
	
	public static final class UIContexts {
		
		private ArrayList<UIContext> uiContexts = new ArrayList<>();
		
		void add(UIContext uiContext) {
			uiContexts.add(uiContext);
		}
		
		public List<UIContext> getUiContexts() {
			return Collections.unmodifiableList(uiContexts);
		}
	}
	
	public static final class UIContext {
		
		private final List<KeyValue> keyValues = new ArrayList<>();

		void add(KeyValue keyValue) {
			keyValues.add(keyValue);
		}
		
		void addAll(Collection<KeyValue> all) {
			keyValues.addAll(all);
		}

		public List<KeyValue> getKeyValues() {
			return keyValues;
		}
		
	}
	
	public static final class KeyValue {
		
		private final String key;
		private final String value;
		
		public KeyValue(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}
		
	}

}
