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
package org.olat.modules.coach.reports;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-01-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public abstract class AbstractReportConfiguration implements ReportConfiguration {
	private Translator translator;

	private String i18nNameKey;
	private String name;
	private String i18nDescriptionKey;
	private String description;
	private String i18nCategoryKey;
	private String category;
	private boolean dynamic;

	protected Translator getTranslator(Locale locale) {
		if (translator == null) {
			translator = Util.createPackageTranslator(AbstractReportConfiguration.class, locale);
		}
		return translator;
	}
	
	@Override
	public String getName(Locale locale) {
		if (StringHelper.containsNonWhitespace(i18nNameKey)) {
			return getTranslator(locale).translate(i18nNameKey);
		}
		return name;
	}

	public void setI18nNameKey(String i18nNameKey) {
		this.i18nNameKey = i18nNameKey;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription(Locale locale) {
		if (StringHelper.containsNonWhitespace(i18nDescriptionKey)) {
			return getTranslator(locale).translate(i18nDescriptionKey);
		}
		return description;
	}

	public void setI18nDescriptionKey(String i18nDescriptionKey) {
		this.i18nDescriptionKey = i18nDescriptionKey;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getCategory(Locale locale) {
		if (StringHelper.containsNonWhitespace(i18nCategoryKey)) {
			return getTranslator(locale).translate(i18nCategoryKey);
		}
		return category;
	}

	public void setI18nCategoryKey(String i18nCategoryKey) {
		this.i18nCategoryKey = i18nCategoryKey;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	@Override
	public abstract void generateReport(Identity coach, Locale locale, List<UserPropertyHandler> userPropertyHandlers);
}
