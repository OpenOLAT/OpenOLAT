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
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.apache.logging.log4j.Logger;

/**
 * Initial date: 2025-01-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public abstract class AbstractReportConfiguration implements ReportConfiguration {
	public static final String PROPS_IDENTIFIER = AbstractReportConfiguration.class.getName();

	protected static final Logger log = Tracing.createLoggerFor(AbstractReportConfiguration.class);

	private String i18nNameKey;
	private String name;
	private String i18nDescriptionKey;
	private String description;
	private boolean dynamic;
	private int order;

	protected Translator getTranslator(Locale locale) {
		return Util.createPackageTranslator(AbstractReportConfiguration.class, locale);
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
		return getTranslator(locale).translate(getI18nCategoryKey());
	}

	protected abstract String getI18nCategoryKey();
	
	@Override
	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	@Override
	public void generateReport(Identity coach, Locale locale) {
		CoachingService coachingService = CoreSpringFactory.getImpl(CoachingService.class);
		List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlers();

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
			generateData(workbook, coach, sheet, userPropertyHandlers, locale);
		} catch (IOException e) {
			log.error("Unable to generate export", e);
			return;
		}

		coachingService.setGeneratedReport(coach, name, fileName);
	}

	protected void generateHeader(OpenXMLWorksheet sheet, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		Translator translator = getTranslator(locale);

		Row header = sheet.newRow();

		int pos = 0;
		pos = generateUserHeaderColumns(header, pos, userPropertyHandlers, translator);
		generateCustomHeaderColumns(header, pos, translator);
	}

	protected int generateUserHeaderColumns(Row header, int pos, List<UserPropertyHandler> userPropertyHandlers, Translator translator) {
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			header.addCell(pos++, translator.translate("export.header." + userPropertyHandler.getName()));
		}
		return pos;
	}

	protected abstract int generateCustomHeaderColumns(Row header, int pos, Translator translator);
	
	protected abstract void generateData(OpenXMLWorkbook workbook, Identity coach, OpenXMLWorksheet sheet, List<UserPropertyHandler> userPropertyHandlers, Locale locale);

	protected abstract List<UserPropertyHandler> getUserPropertyHandlers();

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return order;
	}
}
