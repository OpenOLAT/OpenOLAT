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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.manager.PositionDAO;
import org.olat.modules.selectus.model.ApplicationAttributeLight;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.attributes.AttributeConfiguration;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionComparator;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;

/**
 * 
 * Initial date: 8 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("reportGeneratorUZH")
public class UZHReport implements ReportGenerator {

	private static final Logger log = Tracing.createLoggerFor(UZHReport.class);

	@Override
	public String getFilename(List<Position> positions, Identity identity, Translator translator) {
		String date = Formatter.formatShortDateFilesystem(new Date());
		return "Report_Berufungsverfahren_" + date;
	}

	@Override
	public void generateReport(List<Position> positions, Identity doer, Translator translator, OutputStream out) {
		Translator uTranslator = Util.createPackageTranslator(UZHReport.class, translator.getLocale(), translator);
		new UZHReportGenerator(positions, doer, uTranslator).generateReport(out);
	}
	
	private static class UZHReportGenerator {

		private final Locale locale;
		private final Identity doer;
		private final Translator translator;
		private final List<Position> positions;
		private final List<ApplicationAttributeLight> globalAttributes;
		private final List<PositionAttributeDefinition> globalAttributesDefinitions;
		private final List<PositionAttributeDefinitionConfiguration> globalAttributesConfigurations;
		
		private final List<String> secondAttributesSlots;
		
		@Autowired
		private PositionDAO positionDao;
		@Autowired
		private TaggingService taggingService;
		@Autowired
		private RecruitingService recruitingService;
		@Autowired
		private RecruitingModule recruitingModule;
		@Autowired @Qualifier("salutationGenerator")
		private SalutationGenerator salutationGenerator;
		
		public UZHReportGenerator(List<Position> positions, Identity doer, Translator translator) {
			CoreSpringFactory.autowireObject(this);
			this.locale = translator.getLocale();
			this.doer = doer;
			this.positions = positions;
			this.translator = translator;
			globalAttributes = positionDao.getGlobalAttributes();
			
			List<PositionAttributeDefinition> definitions = recruitingService.getGlobalAttributeDefinition();
			globalAttributesDefinitions = new ArrayList<>(definitions.size());
			for(PositionAttributeDefinition definition:definitions) {
				if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.date
						|| definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.number
						|| definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.percentage
						|| definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.question
						|| definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.select) {
					globalAttributesDefinitions.add(definition);
				}
			}
			
			if(globalAttributesDefinitions.size() > 1) {
				Collections.sort(globalAttributesDefinitions, new PositionAttributeDefinitionComparator());
			}
			
			globalAttributesConfigurations = new ArrayList<>();
			for(PositionAttributeDefinition attributeDefinition:globalAttributesDefinitions) {
				AttributeConfiguration config = attributeDefinition.getConfiguration(AttributeConfiguration.class);
				globalAttributesConfigurations.add(new PositionAttributeDefinitionConfiguration(attributeDefinition,
						attributeDefinition.getTypeEnum(), config));
			}
			
			secondAttributesSlots = new ArrayList<>();
		}
		
		private String translate(String key) {
			return translator.translate(key);
		}
			
		protected void generateReport(OutputStream out) {
			List<String> sheetNames = new ArrayList<>();
			sheetNames.add("Report");
			sheetNames.add("Metadaten");
			
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
			row.addCell(0, translate("report.header.uzh.metadata.system"));
			String url = Settings.createServerURI();
			row.addCell(1, url);

			row = sheet.newRow();
			row.addCell(0, translate("report.header.uzh.metadata.date"));
			row.addCell(1, new Date(), workbook.getStyles().getDateStyle());
			
			row = sheet.newRow();
			row.addCell(0, translate("report.header.uzh.metadata.createBy"));
			if(doer != null) {
				String doerFullname = salutationGenerator.getFullname(doer, locale);
				row.addCell(1, doerFullname);
			}
		}
		
		private void generateHeaders(OpenXMLWorksheet sheet) {
			Row headerRow = sheet.newRow();
			int col = 0;
			// Position informations
			headerRow.addCell(col++, translate("report.header.uzh.position.title"));
			headerRow.addCell(col++, translate("report.header.uzh.position.organisation"));
			headerRow.addCell(col++, translate("report.header.uzh.position.department"));
			
			// Global attributes 1
			for(PositionAttributeDefinition definition:globalAttributesDefinitions) {
				if(!secondAttributesSlots.contains(definition.getLabel(locale))) {
					headerRow.addCell(col++, definition.getLabel(locale, true));
				}
			}
			
			// Committee
			headerRow.addCell(col++, translate("report.header.uzh.committee.professoruzheth.female"));
			headerRow.addCell(col++, translate("report.header.uzh.committee.professoruzheth.male"));
			headerRow.addCell(col++, translate("report.header.uzh.committee.professoruzheth.other"));
			headerRow.addCell(col++, translate("report.header.uzh.committee.professoruzheth.wo"));
			headerRow.addCell(col++, translate("report.header.uzh.committee.external.female"));
			headerRow.addCell(col++, translate("report.header.uzh.committee.external.male"));
			headerRow.addCell(col++, translate("report.header.uzh.committee.external.other"));
			headerRow.addCell(col++, translate("report.header.uzh.committee.external.wo"));
			headerRow.addCell(col++, translate("report.header.uzh.committee.representative.female"));
			headerRow.addCell(col++, translate("report.header.uzh.committee.representative.male"));
			headerRow.addCell(col++, translate("report.header.uzh.committee.representative.other"));
			headerRow.addCell(col++, translate("report.header.uzh.committee.representative.wo"));
			
			// Global attributes 2
			for(PositionAttributeDefinition definition:globalAttributesDefinitions) {
				if(secondAttributesSlots.contains(definition.getLabel(locale))) {
					headerRow.addCell(col++, definition.getLabel(locale, true));
				}
			}
			
			// Applications gender
			headerRow.addCell(col++, translate("report.header.uzh.application.female"));
			headerRow.addCell(col++, translate("report.header.uzh.application.male"));
			headerRow.addCell(col++, translate("report.header.uzh.application.other"));
			headerRow.addCell(col++, translate("report.header.uzh.application.wo"));
			headerRow.addCell(col++, translate("report.header.uzh.application.ab.female"));
			headerRow.addCell(col++, translate("report.header.uzh.application.ab.male"));
			headerRow.addCell(col++, translate("report.header.uzh.application.ab.other"));
			headerRow.addCell(col++, translate("report.header.uzh.application.ab.wo"));
			
			// Applications tags
			headerRow.addCell(col++, translate("report.header.uzh.application.invited.female"));
			headerRow.addCell(col++, translate("report.header.uzh.application.invited.male"));
			headerRow.addCell(col++, translate("report.header.uzh.application.invited.other"));
			headerRow.addCell(col++, translate("report.header.uzh.application.invited.wo"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz1.female"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz1.male"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz1.other"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz1.wo"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz2.female"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz2.male"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz2.other"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz2.wo"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz3.female"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz3.male"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz3.other"));
			headerRow.addCell(col++, translate("report.header.uzh.application.listenplatz3.wo"));
			headerRow.addCell(col++, translate("report.header.uzh.application.berufen.female"));
			headerRow.addCell(col++, translate("report.header.uzh.application.berufen.male"));
			headerRow.addCell(col++, translate("report.header.uzh.application.berufen.other"));
			headerRow.addCell(col++, translate("report.header.uzh.application.berufen.wo"));
		}
		
		private void generateData(Position position, OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
			final CellStyle integerStyle = workbook.getStyles().getIntegerStyle();
			final Row row = sheet.newRow();
			int col = 0;
			
			// Position informations
			row.addCell(col++, position.getPositionTitle(locale));
			row.addCell(col++, position.getOrganisation() == null ? "" : position.getOrganisation().getDisplayName());
			row.addCell(col++, position.getDepartment(locale));
			
			// Global attributes
			for(PositionAttributeDefinitionConfiguration definition:globalAttributesConfigurations) {
				if(!secondAttributesSlots.contains(definition.getAttributeDefinition().getLabel(locale))) {
					addCustomCell(position, definition, row, col, workbook);
					col++;
				}
			}
			
			// Committee
			List<Identity> committee = getRatingsCommittee(position);
			// Only with voting rights
			row.addCell(col++, countIdentity(committee, "female", "professoruzheth"), integerStyle);
			row.addCell(col++, countIdentity(committee, "male", "professoruzheth"), integerStyle);
			row.addCell(col++, countIdentity(committee, "other", "professoruzheth"), integerStyle);
			row.addCell(col++, countIdentity(committee, "-", "professoruzheth"), integerStyle);
			row.addCell(col++, countIdentity(committee, "female", "external"), integerStyle);
			row.addCell(col++, countIdentity(committee, "male", "external"), integerStyle);
			row.addCell(col++, countIdentity(committee, "other", "external"), integerStyle);
			row.addCell(col++, countIdentity(committee, "-", "external"), integerStyle);
			// With and without voting rights
			List<Identity> allCommittee = getCommittee(position);
			row.addCell(col++, countIdentity(allCommittee, "female", "representative"), integerStyle);
			row.addCell(col++, countIdentity(allCommittee, "male", "representative"), integerStyle);
			row.addCell(col++, countIdentity(allCommittee, "other", "representative"), integerStyle);
			row.addCell(col++, countIdentity(allCommittee, "-", "representative"), integerStyle);
			
			// Global attributes
			for(PositionAttributeDefinitionConfiguration definition:globalAttributesConfigurations) {
				if(secondAttributesSlots.contains(definition.getAttributeDefinition().getLabel(locale))) {
					addCustomCell(position, definition, row, col, workbook);
					col++;
				}
			}
			
			// Applications gender
			List<ApplicationLight> applications = recruitingService.getApplications(position);
			row.addCell(col++, countApplications(applications, "f"), integerStyle);
			row.addCell(col++, countApplications(applications, "m"), integerStyle);
			row.addCell(col++, countApplications(applications, "o"), integerStyle);
			row.addCell(col++, countApplications(applications, "-"), integerStyle);
			row.addCell(col++, countABApplications(applications, "f"), integerStyle);
			row.addCell(col++, countABApplications(applications, "m"), integerStyle);
			row.addCell(col++, countABApplications(applications, "o"), integerStyle);
			row.addCell(col++, countABApplications(applications, "-"), integerStyle);
			
			// Applications tags
			List<ApplicationCategoryInfos> tags = taggingService.getApplicationCategories(position, true);
			Map<Long,ApplicationLight> attributesMap = applications.stream()
					.collect(Collectors.toMap(ApplicationLight::getKey, attr -> attr));
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "f", "Eingeladen"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "m", "Eingeladen"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "o", "Eingeladen"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "-", "Eingeladen"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "f", "Listenplatz 1"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "m", "Listenplatz 1"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "o", "Listenplatz 1"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, ".", "Listenplatz 1"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "f", "Listenplatz 2"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "m", "Listenplatz 2"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "o", "Listenplatz 2"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "-", "Listenplatz 2"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "f", "Listenplatz 3"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "m", "Listenplatz 3"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "o", "Listenplatz 3"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "-", "Listenplatz 3"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "f", "Berufen"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "m", "Berufen"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "o", "Berufen"), integerStyle);
			row.addCell(col++, countApplicationWithCategory(tags, attributesMap, "-", "Berufen"), integerStyle);
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
		
		private Integer countApplicationWithCategory(List<ApplicationCategoryInfos> tags, Map<Long,ApplicationLight> attributesMap,
				String gender, String category) {
			Set<Long> applicationKeys = new HashSet<>();
			for(ApplicationCategoryInfos tag:tags) {
				if(category.equalsIgnoreCase(tag.getCategory().getName())) {
					ApplicationLight application = attributesMap.get(tag.getApplicationKey());
					if(application != null && acceptIdentityByGender(application, gender)) {
						applicationKeys.add(application.getKey());
					}
				}
			}
			return Integer.valueOf(applicationKeys.size());
		}
		
		private Integer countApplications(List<ApplicationLight> committee, String gender) {
			long count = committee.stream()
					.filter(c -> acceptIdentityByGender(c, gender))
					.count();
			return Integer.valueOf((int)count);
		}
		
		private Integer countABApplications(List<ApplicationLight> committee, String gender) {
			long count = committee.stream()
					.filter(c -> acceptIdentityByGender(c, gender))
					.filter(c -> Integer.valueOf(3).equals(c.getDecision()) || Integer.valueOf(2).equals(c.getDecision()))
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
		
		private Integer countIdentity(List<Identity> committee, String gender, String typeOfUser) {
			long count = committee.stream()
					.filter(c -> acceptIdentityByGender(c, gender))
					.filter(c -> typeOfUser.equals(c.getUser().getProperty("typeOfUser")))
					.count();
			return Integer.valueOf((int)count);
		}
		
		private static boolean acceptIdentityByGender(Identity identity, String gender) {
			String identityGender = identity.getUser().getProperty(UserConstants.GENDER);
			boolean accept = gender.equals(identityGender)
					|| ("other".equals(gender) && StringHelper.containsNonWhitespace(identityGender)
							&& !"male".equals(identityGender) && !"female".equals(identityGender) && !"-".equals(identityGender) )
					|| ("-".equals(gender) && ("-".equals(identityGender) || !StringHelper.containsNonWhitespace(identityGender)));
			log.debug("Filter identity by gender {} for {} : {}", identityGender, gender, accept);
			return accept;
		}
		
		private List<Identity> getRatingsCommittee(Position position) {
			PositionRole[] ratingsRoles = recruitingModule.getRolesAllowedToRate();
			return recruitingService.getCommittee(position, ratingsRoles);
		}
		
		private List<Identity> getCommittee(Position position) {
			return recruitingService.getCommittee(position, PositionRole.values());
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
