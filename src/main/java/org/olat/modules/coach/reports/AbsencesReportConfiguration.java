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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-01-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AbsencesReportConfiguration extends TimeBoundReportConfiguration {

	@Override
	public boolean hasAccess(ReportConfigurationAccessSecurityCallback secCallback) {
		return secCallback.isCoachingContext();
	}

	@Override
	protected String getI18nCategoryKey() {
		return "report.category.absences";
	}

	@Override
	protected int generateCustomHeaderColumns(Row header, int pos, Translator translator) {
		header.addCell(pos++, translator.translate("export.header.absences"));
		return pos;
	}

	@Override
	protected void generateData(OpenXMLWorkbook workbook, Identity coach, OpenXMLWorksheet sheet, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		LectureService lectureService = CoreSpringFactory.getImpl(LectureService.class);
		LectureStatisticsSearchParameters params = new LectureStatisticsSearchParameters();
		if (getDurationTimeUnit() != null) {
			int duration = getDuration() != null ? Integer.parseInt(getDuration()) : 0;
			params.setStartDate(getDurationTimeUnit().fromDate(new Date(), duration));
			params.setEndDate(getDurationTimeUnit().toDate(new Date()));
		}
		List<LectureBlockIdentityStatistics> statistics = lectureService.getLecturesStatistics(params, userPropertyHandlers, coach);
		for (LectureBlockIdentityStatistics stats : statistics) {
			generateDataRow(sheet, userPropertyHandlers, stats);
		}
	}

	private void generateDataRow(OpenXMLWorksheet sheet, List<UserPropertyHandler> userPropertyHandlers, LectureBlockIdentityStatistics stats) {
		Row row = sheet.newRow();
		int pos = 0;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			row.addCell(pos, stats.getIdentityProp(pos));
			pos++;
		}
		row.addCell(pos, "" + stats.getTotalAbsentLectures());
	}

	@Override
	protected List<UserPropertyHandler> getUserPropertyHandlers() {
		return CoreSpringFactory.getImpl(UserManager.class).getUserPropertyHandlersFor(PROPS_IDENTIFIER, false);
	}
}
