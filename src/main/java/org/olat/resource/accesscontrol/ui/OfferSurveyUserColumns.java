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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.openxml.OpenXMLWorkbookStyles;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.UserPropertiesColumns;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * User columns of the booking form export: the user properties followed by
 * the translated participation status.
 *
 * Initial date: 21 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyUserColumns extends UserPropertiesColumns {

	private final Translator translator;

	public OfferSurveyUserColumns(List<UserPropertyHandler> userPropertyHandlers, Translator translator) {
		super(userPropertyHandlers, translator);
		this.translator = translator;
	}

	@Override
	public void addHeaderColumns(Row row, AtomicInteger col, OpenXMLWorkbookStyles styles) {
		super.addHeaderColumns(row, col, styles);
		row.addCell(col.getAndIncrement(), translator.translate("offer.survey.participation.status"), styles.getBottomAlignStyle());
	}

	@Override
	public void addColumns(EvaluationFormSession session, Row row, AtomicInteger col, OpenXMLWorkbookStyles styles) {
		super.addColumns(session, row, col, styles);
		EvaluationFormParticipation participation = session.getParticipation();
		if (participation != null && participation.getStatus() != null) {
			row.addCell(col.getAndIncrement(), translator.translate("offer.survey.participation.status." + participation.getStatus().name()));
		}
	}

}
