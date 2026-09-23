/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.repository.RepositoryEntry;

/**
 * Excel export of the offer surveys with two response sheets: the completed
 * responses and all responses.
 *
 * Initial date: 23 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyExcelExport extends EvaluationFormExcelExport {
	
	private final Translator acTranslator;

	public OfferSurveyExcelExport(Locale locale, RepositoryEntry formEntry, Form form, SessionFilter filter,
			Comparator<EvaluationFormSession> comparator, UserColumns userColumns, String fileName) {
		super(locale, formEntry, form, filter, comparator, userColumns, fileName);
		acTranslator = Util.createPackageTranslator(OfferSurveyExcelExport.class, locale);
	}

	@Override
	protected List<ResponseSheet> getResponseSheets() {
		return List.of(
				new ResponseSheet(acTranslator.translate("offer.survey.export.sheet.completed"),
						session -> session.getParticipation() != null && session.getParticipation().getStatus() == EvaluationFormParticipationStatus.done),
				new ResponseSheet(acTranslator.translate("offer.survey.export.sheet.all"), session -> true));
	}

}
