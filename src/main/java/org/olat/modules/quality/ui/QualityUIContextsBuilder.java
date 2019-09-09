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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityExecutorParticipation;

/**
 * 
 * Initial date: 29.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class QualityUIContextsBuilder {
	
	public enum Attribute {TOPIC, PREVIOUS_TOPIC, ROLE, COURSE, CURRICULUM_ELEMENTS, TAXONOMY_LEVELS};
	
	protected List<Attribute> attributes = new ArrayList<>();
	protected boolean alwaysEvenKeyValues;
	protected boolean curriculumElementsHierarchy;
	
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
	
	public QualityUIContextsBuilder withCurriculumElementsHierarchy() {
		curriculumElementsHierarchy = true;
		return this;
	}
	
	public static QualityUIContextsBuilder builder(QualityExecutorParticipation participation, Locale locale) {
		return new QualityUIContextsParticipationBuilder(participation, locale);
	}
	
	public static QualityUIContextsBuilder builder(QualityDataCollection dataCollection, Locale locale) {
		return new QualityUIContextsDataCollectionBuilder(dataCollection, locale);
	}
	
	protected QualityUIContextsBuilder() {
		
	}

	public abstract UIContexts build();
	
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
