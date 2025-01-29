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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.apache.logging.log4j.Logger;

/**
 * Initial date: 2025-01-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AbsencesReportConfiguration extends TimeBoundReportConfiguration {

	private static final Logger log = Tracing.createLoggerFor(AbsencesReportConfiguration.class);

	@Override
	public void generateReport(Identity coach, Locale locale, List<UserPropertyHandler> userPropertyHandlers) {
		CoachingService coachingService = CoreSpringFactory.getImpl(CoachingService.class);
		LectureService lectureService = CoreSpringFactory.getImpl(LectureService.class);
		LectureStatisticsSearchParameters params = new LectureStatisticsSearchParameters();
		params.setEndDate(new Date());
		params.setStartDate(getDurationTimeUnit().toDate(new Date(), Integer.valueOf(getDuration())));
		List<LectureBlockIdentityStatistics> statistics = lectureService.getLecturesStatistics(params, userPropertyHandlers, coach);

		LocalFolderImpl folder = coachingService.getGeneratedReportsFolder(coach);
		String name = getName(locale);
		String fileName = StringHelper.transformDisplayNameToFileSystemName(name) + "_" + 
				Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())) + ".xlsx";

		File excelFile = new File(folder.getBasefile(), fileName);
		try (OutputStream out = new FileOutputStream(excelFile); 
			 OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			generateHeader(sheet, userPropertyHandlers, locale);
			
			for (LectureBlockIdentityStatistics stats : statistics) {
				generateDataRow(sheet, userPropertyHandlers, stats);
			}
		} catch (IOException e) {
			log.error("Unable to generate export", e);
			return;
		}
		
		coachingService.setGeneratedReport(coach, name, fileName);
	}

	private void generateHeader(OpenXMLWorksheet sheet, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		Translator translator = Util.createPackageTranslator(AbsencesReportConfiguration.class, locale);
		
		Row header = sheet.newRow();
		int pos = 0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			header.addCell(pos++, translator.translate("export.header." + userPropertyHandler.getName()));
		}
		header.addCell(pos, translator.translate("export.header.absences"));
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
}
