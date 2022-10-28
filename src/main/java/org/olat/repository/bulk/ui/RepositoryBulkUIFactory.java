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
package org.olat.repository.bulk.ui;

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.bulk.model.SettingsSteps;

/**
 * 
 * Initial date: 18 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryBulkUIFactory {
	
	public static String getSettingsDescription(Translator translator, Collection<? extends RepositoryEntryRef> repositoryEntries, String additionalI18nKey) {
		String description = repositoryEntries.size() == 1
				? translator.translate("settings.bulk.edit.single")
				: translator.translate("settings.bulk.edit.multi", String.valueOf(repositoryEntries.size()));
		if (StringHelper.containsNonWhitespace(additionalI18nKey)) {
			description += "<br>";
			description += translator.translate(additionalI18nKey);
		}
		return description;
	}
	
	public static Step getNextSettingsStep(UserRequest ureq, SettingsSteps steps, SettingsSteps.Step currentStep) {
		SettingsSteps.Step nextStep = steps.getNext(currentStep);
		switch (nextStep) {
		case metadata: return new MetadataStep(ureq, steps);
		case taxonomy: return new TaxonomyStep(ureq, steps);
		case organisation: return new OrganisationStep(ureq, steps);
		case authorRights: return new AuthorRightsStep(ureq, steps);
		case overview:
		default: return new SettingsOverviewStep(ureq, steps);
		}
	}

}
