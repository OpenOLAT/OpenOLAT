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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.BeanNameAware;
import org.apache.logging.log4j.Logger;

/**
 * Initial date: 2025-01-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public abstract class AbstractReportConfiguration implements ReportConfiguration, BeanNameAware {
	public static final String PROPS_IDENTIFIER = AbstractReportConfiguration.class.getName();

	protected static final Logger log = Tracing.createLoggerFor(AbstractReportConfiguration.class);

	private String id;
	private String i18nNameKey;
	private String name;
	private String i18nDescriptionKey;
	private String description;
	private boolean dynamic;
	private int order;

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * The identifier of the template is the name of its Spring bean, if not set explicitly.
	 */
	@Override
	public void setBeanName(String name) {
		if (id == null) {
			id = name;
		}
	}

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
	public String getParametersText(ReportParametersText text, Locale locale) {
		return getTranslator(locale).translate(getI18nParametersPrefix() + "." + text.i18nSuffix());
	}
	
	/**
	 * @return The prefix of the i18n keys of the parameters dialog, for example "report.params.absences"
	 */
	protected abstract String getI18nParametersPrefix();
	
	@Override
	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	@Override
	public void generateReport(Identity coach, ReportParameters params, Locale locale, LocalFileImpl output) {
		List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlers();
		List<String> worksheetNames = Arrays.asList("", getMetadataSheetName(locale));
		try (OutputStream out = new FileOutputStream(output.getBasefile());
			 OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, worksheetNames.size(), worksheetNames)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			generateHeader(sheet, userPropertyHandlers, locale);
			generateData(workbook, coach, params, sheet, userPropertyHandlers, locale);
			generateCoachingMetadataSheet(workbook, params, locale);
		} catch (IOException e) {
			throw new ReportGenerationException("Unable to generate export", e);
		}
	}
	
	/**
	 * @param params The parameters of the report
	 * @return The time range of the parameters, all time if the template has no time range
	 */
	protected ReportDateRange resolveRange(ReportParameters params) {
		return ReportDateRange.ALL;
	}
	
	/**
	 * @param locale The locale
	 * @return The name of the last worksheet with the additional information
	 */
	protected String getMetadataSheetName(Locale locale) {
		return getTranslator(locale).translate("report.metadata.sheet");
	}
	
	/**
	 * Adds the worksheet with the additional information of a report started in the coaching tool.
	 * 
	 * @param workbook The workbook, the metadata sheet is the next worksheet
	 * @param params The parameters of the report
	 * @param locale The locale
	 */
	protected void generateCoachingMetadataSheet(OpenXMLWorkbook workbook, ReportParameters params, Locale locale) {
		String exportSource = getTranslator(locale).translate("report.metadata.export.source.coaching");
		generateMetadataSheet(workbook, exportSource, null, params, locale);
	}
	
	/**
	 * Adds the worksheet with the additional information: the export source, the
	 * product or implementation and the time range.
	 * 
	 * @param workbook The workbook, the metadata sheet is the next worksheet
	 * @param exportSource The translated export source, for example "Coaching"
	 * @param productImplementation The product and implementation or null if the report is not limited to one
	 * @param params The parameters of the report
	 * @param locale The locale
	 */
	protected void generateMetadataSheet(OpenXMLWorkbook workbook, String exportSource, String productImplementation,
			ReportParameters params, Locale locale) {
		Translator translator = getTranslator(locale);
		ReportParametersFormatter parametersFormatter = new ReportParametersFormatter(locale);
		OpenXMLWorksheet sheet = workbook.nextWorksheet();
		
		addMetadataRow(workbook, sheet, translator.translate("report.metadata.export.source"), exportSource);
		if (StringHelper.containsNonWhitespace(productImplementation)) {
			addMetadataRow(workbook, sheet, translator.translate("report.metadata.product.implementation"), productImplementation);
		}
		String timeRange = parametersFormatter.formatRange(resolveRange(params), true);
		addMetadataRow(workbook, sheet, translator.translate("report.metadata.time.range"), timeRange);
		if (params != null && params.getChangedSince() != null) {
			String changedSince = Formatter.getInstance(locale).formatDate(params.getChangedSince());
			addMetadataRow(workbook, sheet, translator.translate("report.metadata.changed.since"), changedSince);
		}
	}
	
	private void addMetadataRow(OpenXMLWorkbook workbook, OpenXMLWorksheet sheet, String label, String value) {
		Row row = sheet.newRow();
		row.addCell(0, label, workbook.getStyles().getHeaderStyle());
		row.addCell(1, value);
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
	
	protected abstract void generateData(OpenXMLWorkbook workbook, Identity coach, ReportParameters params, OpenXMLWorksheet sheet, List<UserPropertyHandler> userPropertyHandlers, Locale locale);

	protected abstract List<UserPropertyHandler> getUserPropertyHandlers();

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return order;
	}
}
