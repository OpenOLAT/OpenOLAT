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
package org.olat.modules.quality;

import java.util.Arrays;

import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 15.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum QualityDataCollectionTopicType {
	
	CUSTOM("data.collection.topic.custom"),
	IDENTIY("data.collection.topic.identity"),
	ORGANISATION("data.collection.topic.organisation"),
	CURRICULUM("data.collection.topic.curriculum"),
	CURRICULUM_ELEMENT("data.collection.topic.curriculum.element"),
	REPOSITORY("data.collection.topic.repository");
	
	private final String i18nKey;
	
	private QualityDataCollectionTopicType(String i18nKey) {
		this.i18nKey = i18nKey;
	}
	
	public String getI18nKey() {
		return i18nKey;
	}

	public String getKey() {
		return name();
	}
	
	public static QualityDataCollectionTopicType getEnum(String key) {
		return QualityDataCollectionTopicType.valueOf(key);
	}

	public static String[] getKeys() {
		return Arrays.stream(QualityDataCollectionTopicType.values())
				.map(QualityDataCollectionTopicType::name)
				.toArray(String[]::new);
	}
	
	public static String[] getValues(Translator translator) {
		return Arrays.stream(QualityDataCollectionTopicType.values())
				.map(type -> type.getI18nKey())
				.map(i18n -> translator.translate(i18n))
				.toArray(String[]::new);
	}

}
