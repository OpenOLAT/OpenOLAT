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
package org.olat.modules.coach.security;

import java.util.Locale;

import org.olat.basesecurity.RightProvider;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.coach.ui.CoachMainController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial date: 2025-06-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Component
public class AbsenceReportRightProvider implements RightProvider {

	@Autowired
	private LecturesAndAbsencesRightProvider parentRight;

	public static final String RELATION_RIGHT = "absenceReport";

	@Override
	public String getRight() {
		return RELATION_RIGHT;
	}

	@Override
	public RightProvider getParent() {
		return parentRight;
	}

	@Override
	public boolean isUserRelationsRight() {
		return true;
	}

	@Override
	public int getUserRelationsPosition() {
		return UserRelationRightsOrder.AbsenceReportRight.ordinal();
	}

	@Override
	public int getOrganisationPosition() {
		return OrganisationRightsOrder.AbsenceReportRight.ordinal();
	}

	@Override
	public String getTranslatedName(Locale locale) {
		Translator translator = Util.createPackageTranslator(CoachMainController.class, locale);
		return translator.translate("relation.right.absence.report");
	}
}
