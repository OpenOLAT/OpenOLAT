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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateIdentityConfig;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.manager.CurriculumRepositoryEntryRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-02-04<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CertificatesReportConfiguration extends TimeBoundReportConfiguration {

	@Override
	public boolean hasAccess(ReportConfigurationAccessSecurityCallback secCallback) {
		if (!secCallback.isCoachingContext()) {
			return false;
		}
		
		if (secCallback.isCourseCoach() || secCallback.isCurriculumCoach()) {
			return true;
		}

		if (secCallback.isLineOrEducationManager()) {
			return true;
		}
		
		return false;
	}

	@Override
	protected String getI18nCategoryKey() {
		return "report.category.certificates";
	}

	@Override
	public void generateReport(Identity identity, Locale locale, LocalFileImpl output) {
		Translator translator = getTranslator(locale);
		Formatter formatter = Formatter.getInstance(locale);
		CurriculumModule curriculumModule = CoreSpringFactory.getImpl(CurriculumModule.class);
		boolean curriculumEnabled = curriculumModule.isEnabled();
		int numberOfWorksheets = curriculumEnabled ? 2 : 1;
		List<String> worksheetNames = new ArrayList<>();
		worksheetNames.add(translator.translate("export.worksheet.individual.courses"));
		if (curriculumEnabled) {
			worksheetNames.add(translator.translate("export.worksheet.curricula"));
		}
		
		List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlers();
		try (OutputStream out = new FileOutputStream(output.getBasefile());
			 OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, numberOfWorksheets, worksheetNames)) {
			OpenXMLWorksheet coursesWorksheet = workbook.nextWorksheet();
			generateCoursesHeader(coursesWorksheet, userPropertyHandlers, translator);
			List<CertificateIdentityConfig> certificates = loadCertificates(identity, userPropertyHandlers);
			Map<RepositoryEntryRef, Set<CurriculumElement>> courseToCurriculumElements = new HashMap<>();
			if (curriculumEnabled) {
				Set<RepositoryEntryRef> repositoryEntries = certificates.stream().map(CertificateIdentityConfig::getEntry)
						.filter(Objects::nonNull).map(RepositoryEntry::getKey).map(RepositoryEntryRefImpl::new)
						.collect(Collectors.toSet());
				CurriculumRepositoryEntryRelationDAO curriculumRepositoryEntryRelationDAO = 
						CoreSpringFactory.getImpl(CurriculumRepositoryEntryRelationDAO.class);
				courseToCurriculumElements =
						curriculumRepositoryEntryRelationDAO.getCurriculumElementsForRepositoryEntries(repositoryEntries);
			}
			generateCoursesData(coursesWorksheet, certificates, userPropertyHandlers, formatter, courseToCurriculumElements, translator);
			if (curriculumEnabled) {
				OpenXMLWorksheet curriculaWorksheet = workbook.nextWorksheet();
				curriculaWorksheet.setHeaderRows(1);
				generateCurriculaHeader(curriculaWorksheet, userPropertyHandlers, translator);
				generateCurriculaData(curriculaWorksheet, certificates, userPropertyHandlers, formatter, 
						courseToCurriculumElements, translator);
			}
		} catch (IOException e) {
			log.error("Unable to generate export", e);
		}
	}

	private void generateCoursesHeader(OpenXMLWorksheet coursesWorksheet,
									   List<UserPropertyHandler> userPropertyHandlers, Translator translator) {
		coursesWorksheet.setHeaderRows(1);
		Row header = coursesWorksheet.newRow();
		int pos = 0;
		
		generateCommonCourseHeader(header, pos, userPropertyHandlers, translator);
	}

	private int generateCommonCourseHeader(Row header, int pos, List<UserPropertyHandler> userPropertyHandlers, 
										   Translator translator) {
		header.addCell(pos++, translator.translate("export.header.certificate.id"));
		header.addCell(pos++, translator.translate("export.header.course"));
		header.addCell(pos++, translator.translate("export.header.externalReference"));

		pos = generateUserHeaderColumns(header, pos, userPropertyHandlers, translator);

		header.addCell(pos++, translator.translate("export.header.initialCourseLaunch"));
		header.addCell(pos++, translator.translate("export.header.successState"));
		header.addCell(pos++, translator.translate("export.header.issuedOn"));
		header.addCell(pos, translator.translate("export.header.validUntil"));
		return pos;
	}

	private void generateCurriculaHeader(OpenXMLWorksheet curriculaWorksheet,
										 List<UserPropertyHandler> userPropertyHandlers, Translator translator) {
		curriculaWorksheet.setHeaderRows(1);
		Row header = curriculaWorksheet.newRow();
		int pos = 0;

		header.addCell(pos++, translator.translate("export.header.curricula"));
		header.addCell(pos++, translator.translate("export.header.curriculumType"));
		header.addCell(pos++, translator.translate("export.header.externalReference"));
		
		generateCommonCourseHeader(header, pos, userPropertyHandlers, translator);
	}

	private void generateCoursesData(OpenXMLWorksheet coursesWorksheet,
									 List<CertificateIdentityConfig> certificates,
									 List<UserPropertyHandler> userPropertyHandlers, Formatter formatter,
									 Map<RepositoryEntryRef, Set<CurriculumElement>> courseToCurriculumElements, 
									 Translator translator) {
		certificates.forEach(certificateIdentityConfig -> {

			// Skip certificates that have a reference to a course that is referenced by a curriculum element.
			// These certificates will also appear in the second worksheet, so we skip them here to avoid duplicates.
			RepositoryEntry entry = certificateIdentityConfig.getEntry();
			if (entry != null) {
				RepositoryEntryRef key = new RepositoryEntryRefImpl(certificateIdentityConfig.getEntry().getKey());
				if (courseToCurriculumElements.containsKey(key)) {
					Set<CurriculumElement> curriculumElements = courseToCurriculumElements.get(key);
					if (!curriculumElements.isEmpty()) {
						return;
					}
				}
			}

			Row row = coursesWorksheet.newRow();
			int pos = 0;

			commonCourseData(row, pos, certificateIdentityConfig, userPropertyHandlers, formatter, translator);
		});
	}
	
	private int commonCourseData(Row row, int pos, CertificateIdentityConfig certificateIdentityConfig,
								 List<UserPropertyHandler> userPropertyHandlers, Formatter formatter, Translator translator) {
		
		// certificate ID
		row.addCell(pos++, Long.toString(certificateIdentityConfig.getCertificate().getKey()));
		
		// course
		row.addCell(pos++, certificateIdentityConfig.getCertificate().getCourseTitle());

		// ext. ref.
		if (certificateIdentityConfig.getEntry() != null) {
			row.addCell(pos++, certificateIdentityConfig.getEntry().getExternalRef());
		} else {
			row.addCell(pos++, "");
		}

		// last name, first name, e-mail
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			row.addCell(pos, certificateIdentityConfig.getIdentityProp(i));
			pos++;
		}

		// initial course launch
		if (certificateIdentityConfig.getInitialLaunchDate() != null) {
			row.addCell(pos++, formatter.formatDateAndTime(certificateIdentityConfig.getInitialLaunchDate()));
		} else {
			row.addCell(pos++, "");
		}

		// success state
		row.addCell(pos++, getSuccessState(certificateIdentityConfig, translator));

		// issued on
		row.addCell(pos++, formatter.formatDateAndTime(certificateIdentityConfig.getCertificate().getCreationDate()));

		// valid until
		if (certificateIdentityConfig.getConfig() != null && certificateIdentityConfig.getConfig().isValidityEnabled()) {
			Date validUntil = certificateIdentityConfig.getConfig().getValidityTimelapseUnit()
					.toDate(certificateIdentityConfig.getCertificate().getCreationDate(), 
							certificateIdentityConfig.getConfig().getValidityTimelapse());
			row.addCell(pos++, formatter.formatDateAndTime(validUntil));
		} else {
			row.addCell(pos++, "");
		}
		
		return pos;
	}
	
	private String getSuccessState(CertificateIdentityConfig certificateIdentityConfig, Translator translator) {
		if (certificateIdentityConfig.getPassed() == null) {
			return translator.translate("export.value.undefined");
		}
		return certificateIdentityConfig.getPassed() ? translator.translate("export.value.passed") : translator.translate("export.value.failed");
	}

	private List<CertificateIdentityConfig> loadCertificates(Identity identity, 
															 List<UserPropertyHandler> userPropertyHandlers) {
		Date from = null;
		Date to = null;
		if (getDurationTimeUnit() != null) {
			int duration = getDuration() != null ? Integer.parseInt(getDuration()) : 0;
			from = getDurationTimeUnit().fromDate(new Date(), duration);
			to = getDurationTimeUnit().toDate(new Date());
		}

		CertificatesManager certificatesManager = CoreSpringFactory.getImpl(CertificatesManager.class);
		
		List<CertificateIdentityConfig> groupCertificates =
				certificatesManager.getCertificatesForGroups(identity, userPropertyHandlers, from, to);
		List<CertificateIdentityConfig> orgCertificates =
				certificatesManager.getCertificatesForOrganizations(identity, userPropertyHandlers, from, to);

		Set<CertificateIdentityConfig> certificates = Stream.concat(groupCertificates.stream(), orgCertificates.stream()).collect(Collectors.toSet());
		certificatesManager.enhanceCertificatesWithPassedInformation(certificates);
		
		return certificates.stream().toList();
	}

	private void generateCurriculaData(OpenXMLWorksheet curriculaWorksheet,
									   List<CertificateIdentityConfig> certificates,
									   List<UserPropertyHandler> userPropertyHandlers, Formatter formatter,
									   Map<RepositoryEntryRef, Set<CurriculumElement>> courseToCurriculumElements, 
									   Translator translator) {
		certificates.forEach(certificateIdentityConfig -> {
			RepositoryEntry entry = certificateIdentityConfig.getEntry();
			if (entry == null) {
				return;
			}
			RepositoryEntryRef key = new RepositoryEntryRefImpl(certificateIdentityConfig.getEntry().getKey());
			if (!courseToCurriculumElements.containsKey(key)) {
				return;
			}
			Set<CurriculumElement> curriculumElements = courseToCurriculumElements.get(key);
			if (curriculumElements.isEmpty()) {
				return;
			}

			Row row = curriculaWorksheet.newRow();
			int pos = 0;

			// curriculum element names
			String names = curriculumElements.stream().map(CurriculumElement::getDisplayName)
					.collect(Collectors.joining("|"));
			row.addCell(pos++, names);
			
			// curriculum element types
			String types = curriculumElements.stream().map(CurriculumElement::getType)
					.map(CurriculumElementType::getIdentifier).collect(Collectors.joining("|"));
			row.addCell(pos++, types);
			
			// curriculum element external references
			String externalReferences = curriculumElements.stream().map(CurriculumElement::getExternalId)
					.collect(Collectors.joining("|"));
			row.addCell(pos++, externalReferences);

			commonCourseData(row, pos, certificateIdentityConfig, userPropertyHandlers, formatter, translator);
		});
	}

	@Override
	protected int generateCustomHeaderColumns(Row header, int pos, Translator translator) {
		// Return default.
		// Two worksheets used, this hook is for a single worksheet.
		return 0;
	}

	@Override
	protected void generateData(OpenXMLWorkbook workbook, Identity coach, OpenXMLWorksheet sheet,
								List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		// Unused.
		// Two worksheets used, this hook is for a single worksheet.
	}

	@Override
	protected List<UserPropertyHandler> getUserPropertyHandlers() {
		return CoreSpringFactory.getImpl(UserManager.class)
				.getUserPropertyHandlersFor(CertificatesReportConfiguration.class.getName(), false);
	}
}
