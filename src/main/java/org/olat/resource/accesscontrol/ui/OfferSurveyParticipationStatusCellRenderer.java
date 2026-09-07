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
package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.forms.EvaluationFormParticipationStatus;

/**
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyParticipationStatusCellRenderer extends LabelCellRenderer {

	@Override
	protected String getCellValue(Object val, Translator translator) {
		if (val instanceof EvaluationFormParticipationStatus status) {
			return translator.translate("offer.survey.participation.status." + status.name());
		}
		return "";
	}

	@Override
	protected String getIconCssClass(Object val) {
		return null;
	}

	@Override
	protected String getElementCssClass(Object val) {
		if (val instanceof EvaluationFormParticipationStatus status) {
			return switch (status) {
			case prepared -> "o_offer_survey_status_prepared";
			case done -> "o_offer_survey_status_done";
			case canceled -> "o_offer_survey_status_canceled";
			};
		}
		return null;
	}

}
