/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.report;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.olat.modules.selectus.model.ApplicationAttributeLight;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.PersonGender;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service("reportGeneratorBasic")
public class BasicReport implements ReportGenerator {

	private static final Logger log = Tracing.createLoggerFor(BasicReport.class);
	
	@Override
	public String getFilename(List<Position> positions, Identity identity, Translator translator) {
		String date = Formatter.formatShortDateFilesystem(new Date());
		return "Report_" + date;
	}

	@Override
	public void generateReport(List<Position> positions, Identity doer, Translator translator, OutputStream out) {
		Translator uTranslator = Util.createPackageTranslator(ReportGenerator.class, translator.getLocale(), translator);
		new ReportGenerator(positions, doer, uTranslator).generateReport(out);
	}
	
	private static class ReportGenerator extends AbstractReportGenerator {

		private final Locale locale;
		private final Identity doer;
		private final Translator translator;
		private final List<Position> positions;
		private final PersonGender[] personGenders;

		public ReportGenerator(List<Position> positions, Identity doer, Translator translator) {
			super();
			
			this.locale = translator.getLocale();
			this.doer = doer;
			this.positions = positions;
			this.translator = translator;
			personGenders = recruitingModule.getPersonGenders();
		}
		
		private String translate(String key) {
			return translator.translate(key);
		}
			
		protected void generateReport(OutputStream out) {
			List<String> sheetNames = new ArrayList<>();
			sheetNames.add(translate("report.header.report"));
			sheetNames.add(translate("report.header.metadata"));
			
			try (OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 2, sheetNames)) {
				OpenXMLWorksheet sheet = workbook.nextWorksheet();
				sheet.setHeaderRows(1);
				generateHeaders(sheet);
				for(Position position:positions) {
					generateData(position, sheet, workbook);
				}
				
				OpenXMLWorksheet metaSheet = workbook.nextWorksheet();
				generateMetadatens(metaSheet, workbook);
			} catch (Exception e) {
				log.error("Unable to generate report", e);
			}
		}
		
		private void generateMetadatens(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
			Row headerRow = sheet.newRow();
			headerRow.addCell(0, translate("report.header.uzh.metadata.title"), workbook.getStyles().getHeaderStyle());
			
			Row row = sheet.newRow();
			row.addCell(0, translate("report.header.metadata.system"));
			String url = Settings.createServerURI();
			row.addCell(1, url);

			row = sheet.newRow();
			row.addCell(0, translate("report.header.metadata.date"));
			row.addCell(1, new Date(), workbook.getStyles().getDateStyle());
			
			row = sheet.newRow();
			row.addCell(0, translate("report.header.metadata.createBy"));
			if(doer != null) {
				String doerFullname = salutationGenerator.getFullname(doer, locale);
				row.addCell(1, doerFullname);
			}
		}
		
		private void generateHeaders(OpenXMLWorksheet sheet) {
			final boolean hasOther = PersonGender.hasValue(personGenders, PersonGender.other);
			Row headerRow = sheet.newRow();
			
			int col = 0;
			// Position informations
			headerRow.addCell(col++, translate("report.header.position.title"));
			
			// Applications gender
			headerRow.addCell(col++, translate("report.header.application.female"));
			headerRow.addCell(col++, translate("report.header.application.male"));
			if(hasOther) {
				headerRow.addCell(col++, translate("report.header.application.other"));
			}
			headerRow.addCell(col++, translate("report.header.application.wo"));
			
			headerRow.addCell(col++, translate("report.header.application.a.female"));
			headerRow.addCell(col++, translate("report.header.application.a.male"));
			if(hasOther) {
				headerRow.addCell(col++, translate("report.header.application.a.other"));
			}
			headerRow.addCell(col++, translate("report.header.application.a.wo"));
			
			headerRow.addCell(col++, translate("report.header.application.b.female"));
			headerRow.addCell(col++, translate("report.header.application.b.male"));
			if(hasOther) {
				headerRow.addCell(col++, translate("report.header.application.b.other"));
			}
			headerRow.addCell(col++, translate("report.header.application.b.wo"));
			
			headerRow.addCell(col++, translate("report.header.application.c.female"));
			headerRow.addCell(col++, translate("report.header.application.c.male"));
			if(hasOther) {
				headerRow.addCell(col++, translate("report.header.application.c.other"));
			}
			headerRow.addCell(col++, translate("report.header.application.c.wo"));
			
			// Global attributes
			for(PositionAttributeDefinition definition:globalAttributesDefinitions) {
				headerRow.addCell(col++, definition.getLabel(locale, true));
			}
		}
		
		private void generateData(Position position, OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
			final boolean hasOther = PersonGender.hasValue(personGenders, PersonGender.other);
			final CellStyle integerStyle = workbook.getStyles().getIntegerStyle();
			final Row row = sheet.newRow();
			int col = 0;
			
			// Position informations
			row.addCell(col++, position.getPositionTitle(locale));
			
			// Applications gender
			List<ApplicationLight> applications = recruitingService.getApplications(position);
			row.addCell(col++, countApplications(applications, "f"), integerStyle);
			row.addCell(col++, countApplications(applications, "m"), integerStyle);
			if(hasOther) {
				row.addCell(col++, countApplications(applications, "o"), integerStyle);
			}
			row.addCell(col++, countApplications(applications, "-"), integerStyle);
			
			Integer decisionA = Integer.valueOf(3);
			row.addCell(col++, countApplications(applications, decisionA, "f"), integerStyle);
			row.addCell(col++, countApplications(applications, decisionA, "m"), integerStyle);
			if(hasOther) {
				row.addCell(col++, countApplications(applications, decisionA, "o"), integerStyle);
			}
			row.addCell(col++, countApplications(applications, decisionA, "-"), integerStyle);
			
			Integer decisionB = Integer.valueOf(2);
			row.addCell(col++, countApplications(applications, decisionB, "f"), integerStyle);
			row.addCell(col++, countApplications(applications, decisionB, "m"), integerStyle);
			if(hasOther) {
				row.addCell(col++, countApplications(applications, decisionB, "o"), integerStyle);
			}
			row.addCell(col++, countApplications(applications, decisionB, "-"), integerStyle);
			
			Integer decisionC = Integer.valueOf(1);
			row.addCell(col++, countApplications(applications, decisionC, "f"), integerStyle);
			row.addCell(col++, countApplications(applications, decisionC, "m"), integerStyle);
			if(hasOther) {
				row.addCell(col++, countApplications(applications, decisionC, "o"), integerStyle);
			}
			row.addCell(col++, countApplications(applications, decisionC, "-"), integerStyle);
			
			// Global attributes
			for(PositionAttributeDefinitionConfiguration definition:globalAttributesConfigurations) {
				addCustomCell(position, definition, row, col, workbook);
				col++;
			}
		}
		
		private void addCustomCell(Position position, PositionAttributeDefinitionConfiguration definition,
				Row row, int col, OpenXMLWorkbook workbook) {
			Object obj = localizedAdditionalValues(position, definition);
			if(definition.getAttributeDefinition().getTypeEnum() == PositionAttributeDefinitionTypeEnum.number && obj instanceof Number) {
				row.addCell(col, (Number)obj, workbook.getStyles().getIntegerStyle());
			} else if(definition.getAttributeDefinition().getTypeEnum() == PositionAttributeDefinitionTypeEnum.percentage && obj instanceof Number) {
				double num = ((Number)obj).doubleValue() / 100d;
				row.addCell(col, Double.valueOf(num), workbook.getStyles().getPercentStyle());
			}  else if(definition.getAttributeDefinition().getTypeEnum() == PositionAttributeDefinitionTypeEnum.date && obj instanceof Date) {
				row.addCell(col, (Date)obj, workbook.getStyles().getDateStyle());
			} else if(obj instanceof String) {
				row.addCell(col, (String)obj);
			} else if(obj instanceof String[]) {
				row.addCell(col, String.join(", ", (String[])obj));
			} 
		}
		
		private Integer countApplications(List<ApplicationLight> committee, String gender) {
			long count = committee.stream()
					.filter(c -> acceptIdentityByGender(c, gender))
					.count();
			return Integer.valueOf((int)count);
		}
		
		private Integer countApplications(List<ApplicationLight> committee, Integer decision, String gender) {
			long count = committee.stream()
					.filter(c -> acceptIdentityByGender(c, gender))
					.filter(c -> decision.equals(c.getDecision()))
					.count();
			return Integer.valueOf((int)count);
		}
		
		private static boolean acceptIdentityByGender(ApplicationLight application, String gender) {
			String applicantGender = application.getPerson().getGender();
			boolean accept = gender.equals(applicantGender)
					|| ("o".equals(gender) && StringHelper.containsNonWhitespace(applicantGender)
							&& !"m".equals(applicantGender) && !"f".equals(applicantGender) && !"-".equals(applicantGender))
					|| ("-".equals(gender) && ("-".equals(applicantGender) || !StringHelper.containsNonWhitespace(applicantGender)));
			log.debug("Filter applicant by gender {} for {} : {}", applicantGender, gender, accept);
			return accept;
		}
		
		private Object localizedAdditionalValues(Position position, PositionAttributeDefinitionConfiguration selectConfiguration) {
			for(ApplicationAttributeLight globalAttribute:globalAttributes) {
				if(position.getKey().equals(globalAttribute.getPositionKey())
						&& selectConfiguration.getDefinitionKey().equals(globalAttribute.getDefinitionKey())) {
					return ApplicationAttributesDelegate.getLocalizedValuesWithOthers(selectConfiguration, globalAttribute.getValue(), locale);
				}
			}
			return null;
		}
	}
}
