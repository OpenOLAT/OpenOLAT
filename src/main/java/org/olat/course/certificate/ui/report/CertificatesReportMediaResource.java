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
package org.olat.course.certificate.ui.report;

import java.io.OutputStream;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.course.CourseFactory;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.report.CertificatesReportParameters.With;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 11 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatesReportMediaResource extends OpenXMLWorkbookResource {
	
	private static final Logger log = Tracing.createLoggerFor(CertificatesReportMediaResource.class);
	

	private static final String usageIdentifyer = CertificatesReportMediaResource.class.getName();
	
	private final Date now = new Date();
	private final Translator translator;
	private final List<RepositoryEntry> entries;
	private List<UserPropertyHandler> userPropertyHandlers;
	private final CertificatesReportParameters reportParams;
	private final AssessmentToolSecurityCallback secCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, null, null);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private UserCourseInformationsManager userInfosMgr;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public CertificatesReportMediaResource(String label, List<RepositoryEntry> entries,
			CertificatesReportParameters reportParams, Translator translator) {
		super(label);
		CoreSpringFactory.autowireObject(this);
		this.entries = entries;
		this.reportParams = reportParams;
		this.translator = userManager.getPropertyHandlerTranslator(translator);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, true);
	}
	
	@Override
	protected void generate(OutputStream out) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			generateHeaders(sheet);
			generateData(sheet, workbook);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	protected void generateHeaders(OpenXMLWorksheet sheet) {
		Row headerRow = sheet.newRow();
		int col = 0;
		
		// course
		headerRow.addCell(col++, translator.translate("report.course.displayname"));
		
		// user properties
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			if(userPropertyHandler == null) continue;
			headerRow.addCell(col++, translator.translate(userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}
		
		//passed
		headerRow.addCell(col++, translator.translate("report.course.passed"));
		
		// initial launch date
		headerRow.addCell(col++, translator.translate("report.initialLaunchDate"));
		
		// certificate
		headerRow.addCell(col, translator.translate("report.certificate"));
	}
	
	protected void generateData(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		Collections.sort(entries, new RepositoryEntryComparator(translator.getLocale()));
		
		for(RepositoryEntry entry:entries) {
			try {
				generateData(entry, sheet, workbook);
			} catch (Exception e) {
				log.error("", e);
			} finally {
				dbInstance.commitAndCloseSession();
			}
		}	
	}
	
	protected void generateData(RepositoryEntry entry, OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {	
		OLATResource resource = entry.getOlatResource();
		List<CertificateLight> certificates = certificatesManager.getLastCertificates(resource);
		Map<Long, CertificateLight> certificateMap = certificates.stream()
				.collect(Collectors.toMap(CertificateLight::getIdentityKey, Function.identity()));
		
		Map<Long, Date> initialLaunchDates = userInfosMgr
				.getInitialLaunchDates(resource);
		
		String rootIdent = CourseFactory.loadCourse(entry).getRunStructure().getRootNode().getIdent();
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, rootIdent, null, secCallback);
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(null, params);
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(null, params, null);
		Map<Long,AssessmentEntry> entryMap = new HashMap<>();
		assessmentEntries.stream()
			.filter(assessmentEntry -> assessmentEntry.getIdentity() != null)
			.forEach(assessmentEntry -> entryMap.put(assessmentEntry.getIdentity().getKey(), assessmentEntry));
		
		dbInstance.commitAndCloseSession();
		
		for(Identity participant:assessedIdentities) {
			AssessmentEntry assessmentEntry = entryMap.get(participant.getKey());
			CertificateLight certificate = certificateMap.get(participant.getKey());
			if(!accept(assessmentEntry, certificate)) {
				continue;
			}
			
			Date launchDate = initialLaunchDates.get(participant.getKey());
			
			int col = 0;
			Row row = sheet.newRow();
			row.addCell(col++, entry.getDisplayname());
			
			for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				if(userPropertyHandler == null) continue;

				String val = participant.getUser().getProperty(userPropertyHandler.getName(), translator.getLocale());
				row.addCell(col++, val);
			}
			
			//passed
			if(assessmentEntry == null || assessmentEntry.getPassed() == null) {
				col++;
			} else {
				String val = assessmentEntry.getPassed().booleanValue()
						? translator.translate("report.course.passed.passed") : translator.translate("report.course.passed.failed");
				row.addCell(col++, val);
			}
			
			row.addCell(col++, launchDate, workbook.getStyles().getDateStyle());
			
			if(certificate != null) {
				row.addCell(col, certificate.getCreationDate(), workbook.getStyles().getDateStyle());
			}
		}
	}
	
	private boolean accept(AssessmentEntry assessmentEntry, CertificateLight certificate) {
		if(reportParams.getCertificateStart() != null && (certificate == null || certificate.getCreationDate().before(reportParams.getCertificateStart()))) {
			return false;	
		}
		if(reportParams.getCertificateEnd() != null && (certificate == null || certificate.getCreationDate().after(reportParams.getCertificateEnd()))) {
			return false;	
		}
		if(reportParams.isOnlyPassed() && (assessmentEntry == null || assessmentEntry.getPassed() == null || !assessmentEntry.getPassed().booleanValue())) {
			return false;
		}
		
		List<With> withList = reportParams.getWith();
		if(withList == null || withList.isEmpty()) {
			return true;
		}
		
		for(With with:withList) {
			if(with == With.withoutCertificate && certificate == null) {
				return true;
			}
			if(with == With.validCertificate && (certificate != null && (certificate.getNextRecertificationDate() == null || certificate.getNextRecertificationDate().after(now)))) {
				return true;
			}
			if(with == With.expiredCertificate && (certificate != null && (certificate.getNextRecertificationDate() != null || certificate.getNextRecertificationDate().before(now)))) {
				return true;
			}
		}
		
		return false;
	}
	
	private static class RepositoryEntryComparator implements Comparator<RepositoryEntry> {
		
		private final Collator collator;
		
		public RepositoryEntryComparator(Locale locale) {
			collator = Collator.getInstance(locale);
		}
		
		@Override
		public int compare(RepositoryEntry o1, RepositoryEntry o2) {
			String d1 = o1.getDisplayname();
			String d2 = o2.getDisplayname();
			int c = collator.compare(d1, d2);
			if(c == 0) {
				c = o1.getKey().compareTo(o2.getKey());
			}
			return c;
		}
	}
}
