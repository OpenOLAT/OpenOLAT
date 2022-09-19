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
package org.olat.course.learningpath;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 20 Jan 2020<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathTranslationsBuilder {

	static final LearningPathTranslations UNTRANSLATED = new Untranslated();

	private final LearningPathEditConfigsBuilder editConfigsBuilder;
	private Class<?> translatorBaseClass;
	private String triggerStatusInReviewKey;
	private String triggerStatusDoneKey;
	private String triggerNodeCompletedKey;

	LearningPathTranslationsBuilder(LearningPathEditConfigsBuilder editConfigsBuilder) {
		this.editConfigsBuilder = editConfigsBuilder;
	}
	
	public LearningPathTranslationsBuilder withTranslatorBaseClass(Class<?> baseClass) {
		this.translatorBaseClass = baseClass;
		return this;
	}

	public LearningPathTranslationsBuilder withTriggerStatusInReview(String i18nKey) {
		this.triggerStatusInReviewKey = i18nKey;
		return this;
	}

	public LearningPathTranslationsBuilder withTriggerStatusDone(String i18nKey) {
		this.triggerStatusDoneKey = i18nKey;
		return this;
	}

	public LearningPathTranslationsBuilder withTriggerNodeCompleted(String i18nKey) {
		this.triggerNodeCompletedKey = i18nKey;
		return this;
	}
	
	public LearningPathEditConfigsBuilder buildTranslations() {
		return editConfigsBuilder;
	}

	LearningPathTranslations build() {
		return translatorBaseClass != null
				? new LearningPathTranslationsImpl(this)
				: UNTRANSLATED;
	}

	private static final class LearningPathTranslationsImpl implements LearningPathTranslations {

		private final Class<?> translatorBaseClass;
		private final String triggerStatusInReviewKey;
		private final String triggerStatusDoneKey;
		private final String triggerNodeCompletedKey;
		
		private LearningPathTranslationsImpl(LearningPathTranslationsBuilder builder) {
			this.translatorBaseClass = builder.translatorBaseClass;
			this.triggerStatusInReviewKey = builder.triggerStatusInReviewKey;
			this.triggerStatusDoneKey = builder.triggerStatusDoneKey;
			this.triggerNodeCompletedKey = builder.triggerNodeCompletedKey;
		}

		@Override
		public String getTriggerStatusInReview(Locale locale) {
			return StringHelper.containsNonWhitespace(triggerStatusInReviewKey)
					? getTranslator(locale).translate(triggerStatusInReviewKey)
					: null;
		}

		@Override
		public String getTriggerStatusDone(Locale locale) {
			return StringHelper.containsNonWhitespace(triggerStatusDoneKey)
					? getTranslator(locale).translate(triggerStatusDoneKey)
					: null;
		}

		@Override
		public String getTriggerNodeCompleted(Locale locale) {
			return StringHelper.containsNonWhitespace(triggerNodeCompletedKey)
					? getTranslator(locale).translate(triggerNodeCompletedKey)
					: null;
		}

		private Translator getTranslator(Locale locale) {
			return Util.createPackageTranslator(translatorBaseClass, locale);
		}
	}
	
	private static final class Untranslated implements LearningPathTranslations {

		@Override
		public String getTriggerStatusInReview(Locale locale) {
			return null;
		}

		@Override
		public String getTriggerStatusDone(Locale locale) {
			return null;
		}

		@Override
		public String getTriggerNodeCompleted(Locale locale) {
			return null;
		}
		
	}
}
