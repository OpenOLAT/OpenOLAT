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
package org.olat.course.certificate.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.pdf.PdfDocument;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Do the hard work of filling the certificate
 * 
 * Initial date: 23.10.2014<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatePDFFormWorker {

	private static final Logger log = Tracing
			.createLoggerFor(CertificatePDFFormWorker.class);
	
	private final Float score;
	private final Float maxScore;
	private final Boolean passed;
	private final Double completion;
	private final Identity identity;
	private final RepositoryEntry entry;

	private final Date dateCertification;
	private final Date dateFirstCertification;
	private final Date dateNextRecertification;
	private final String custom1;
	private final String custom2;
	private final String custom3;
	private final String certificateURL;

	private final Locale locale;
	private final UserManager userManager;
	private final CertificatesManagerImpl certificatesManager;

	public CertificatePDFFormWorker(Identity identity, RepositoryEntry entry, Float score, Float maxScore, Boolean passed,
			Double completion, Date dateCertification, Date dateFirstCertification, Date dateNextRecertification, String custom1,
			String custom2, String custom3, String certificateURL, Locale locale, UserManager userManager,
			CertificatesManagerImpl certificatesManager) {
		this.entry = entry;
		this.score = score;
		this.maxScore = maxScore;
		this.passed = passed;
		this.completion = completion;
		this.custom1 = custom1;
		this.custom2 = custom2;
		this.custom3 = custom3;
		this.locale = locale;
		this.identity = identity;
		this.dateCertification = dateCertification;
		this.dateFirstCertification = dateFirstCertification;
		this.dateNextRecertification = dateNextRecertification;
		this.certificateURL = certificateURL;
		this.userManager = userManager;
		this.certificatesManager = certificatesManager;
	}

	public File fill(CertificateTemplate template, File destinationDir, String certificateFilename) {
		PDDocument document = null;
		InputStream templateStream = null;
		try {
			File templateFile = null;
			if(template != null) {
				templateFile = certificatesManager.getTemplateFile(template);
			}
			
			if(templateFile != null && templateFile.exists()) {
				templateStream = new FileInputStream(templateFile);
			} else {
				templateStream = CertificatesManager.class.getResourceAsStream("template.pdf");
			}
			
			document = PDDocument.load(templateStream);

			PDDocumentCatalog docCatalog = document.getDocumentCatalog();
			PDAcroForm acroForm = docCatalog.getAcroForm();
			if (acroForm != null) {
				fillUserProperties(acroForm);
				fillRepositoryEntry(acroForm);
				fillCertificationInfos(acroForm);
				fillAssessmentInfos(acroForm);
				fillMetaInfos(acroForm);
			}
			if(!destinationDir.exists()) {
				destinationDir.mkdirs();
			}
			
			File certificateFile = new File(destinationDir, certificateFilename);
			OutputStream out = new FileOutputStream(certificateFile);
			document.save(out);
			out.flush();
			out.close();
			return certificateFile;
		} catch (Exception e) {
			log.error("", e);
			return null;
		} finally {
			IOUtils.closeQuietly(document);
			IOUtils.closeQuietly(templateStream);
		}
	}

	private void fillUserProperties(PDAcroForm acroForm) throws IOException {
		User user = identity.getUser();
		List<UserPropertyHandler> userPropertyHandlers = userManager.getAllUserPropertyHandlers();
		for (UserPropertyHandler handler : userPropertyHandlers) {
			String propertyName = handler.getName();
			String value = handler.getUserProperty(user, locale);
			fillField(propertyName, value, acroForm);
		}
		
		String fullName = userManager.getUserDisplayName(identity);
		fillField("fullName", fullName, acroForm);
		
		String firstName = user.getProperty(UserConstants.FIRSTNAME, locale);
		String lastName = user.getProperty(UserConstants.LASTNAME, locale);
		
		StringBuilder firstNameLastName = new StringBuilder();
		StringBuilder lastNameFirstName = new StringBuilder();
		if(StringHelper.containsNonWhitespace(firstName)) {
			firstNameLastName.append(firstName);
		}
		if(StringHelper.containsNonWhitespace(lastName)) {
			if(firstNameLastName.length() > 0) firstNameLastName.append(" ");
			firstNameLastName.append(lastName);
			lastNameFirstName.append(lastName);
		}
		if(StringHelper.containsNonWhitespace(firstName)) {
			if(lastNameFirstName.length() > 0) lastNameFirstName.append(" ");
			lastNameFirstName.append(firstName);
		}
		fillField("firstNameLastName", firstNameLastName.toString(), acroForm);
		fillField("lastNameFirstName", lastNameFirstName.toString(), acroForm);
	}
	
	private String filterHtml(String text) {
		if(!StringHelper.containsNonWhitespace(text)) {
			return "";
		}
		return FilterFactory.getHtmlTagAndDescapingFilter().filter(text);
	}

	private void fillRepositoryEntry(PDAcroForm acroForm) throws IOException {
		String title = entry.getDisplayname();
		fillField("title", title, acroForm);
		String description = entry.getDescription();
		fillField("description", filterHtml(description), acroForm);
		String requirements = entry.getRequirements();
		fillField("requirements", filterHtml(requirements), acroForm);
		String objectives = entry.getObjectives();
		fillField("objectives", filterHtml(objectives), acroForm);
		String credits = entry.getCredits();
		fillField("credits", filterHtml(credits), acroForm);
		String externalRef = entry.getExternalRef();
		fillField("externalReference", filterHtml(externalRef), acroForm);
		String authors = entry.getAuthors();
		fillField("authors", authors, acroForm);
		String expenditureOfWorks = entry.getExpenditureOfWork();
		fillField("expenditureOfWork", expenditureOfWorks, acroForm);
		fillField("expenditureOfWorks", expenditureOfWorks, acroForm);
		String mainLanguage = entry.getMainLanguage();
		fillField("mainLanguage", mainLanguage, acroForm);
		String location = entry.getLocation();
		fillField("location", location, acroForm);

		if (entry.getLifecycle() != null) {
			Formatter format = Formatter.getInstance(locale);

			Date from = entry.getLifecycle().getValidFrom();
			String formattedFrom = format.formatDate(from);
			fillField("from", formattedFrom, acroForm);
			String formattedFromLong = format.formatDateLong(from);
			fillField("fromLong", formattedFromLong, acroForm);

			Date to = entry.getLifecycle().getValidTo();
			String formattedTo = format.formatDate(to);
			fillField("to", formattedTo, acroForm);
			String formattedToLong = format.formatDateLong(to);
			fillField("toLong", formattedToLong, acroForm);
		}
	}
	
	private void fillCertificationInfos(PDAcroForm acroForm) throws IOException {
		Formatter format = Formatter.getInstance(locale);

		if(dateCertification == null) {
			fillField("dateCertification", "", acroForm);
		} else {
			String formattedDateCertification = format.formatDate(dateCertification);
			fillField("dateCertification", formattedDateCertification, acroForm);
			String formattedDateCertificationLong = format.formatDateLong(dateCertification);
			fillField("dateCertificationLong", formattedDateCertificationLong, acroForm);		
		}
		
		if(dateFirstCertification == null) {
			fillField("dateFirstCertification", "", acroForm);
		} else {
			String formattedDateFirstCertification = format.formatDate(dateFirstCertification);
			fillField("dateFirstCertification", formattedDateFirstCertification, acroForm);
			String formattedDateFirstCertificationLong = format.formatDate(dateFirstCertification);
			fillField("dateFirstCertificationLong", formattedDateFirstCertificationLong, acroForm);		
		}
		
		if(dateNextRecertification == null) {
			fillField("dateNextRecertification", "", acroForm);
		} else {
			String formattedDateNextRecertification = format.formatDate(dateNextRecertification);
			fillField("dateNextRecertification", formattedDateNextRecertification, acroForm);
			String formattedDateNextRecertificationLong = format.formatDateLong(dateNextRecertification);
			fillField("dateNextRecertificationLong", formattedDateNextRecertificationLong, acroForm);
		}		

	}
	
	private void fillAssessmentInfos(PDAcroForm acroForm) throws IOException {
		String roundedScore = AssessmentHelper.getRoundedScore(score);
		fillField("score", roundedScore, acroForm);
		
		String roundedMaxScore = AssessmentHelper.getRoundedScore(maxScore);
		fillField("maxScore", roundedMaxScore, acroForm);

		String status = (passed != null && passed.booleanValue()) ? "Passed" : "Failed";
		fillField("status", status, acroForm);
		
		String roundedCompletion = completion != null? Formatter.roundToString(completion * 100, 0): null;
		fillField("progress", roundedCompletion, acroForm);
	}
	
	private void fillMetaInfos(PDAcroForm acroForm) throws IOException {
		fillField("custom1", custom1, acroForm);
		fillField("custom2", custom2, acroForm);
		fillField("custom3", custom3, acroForm);
		fillField("certificateVerificationUrl", certificateURL, acroForm);
	}

	private void fillField(String fieldName, String value, PDAcroForm acroForm)
			throws IOException {
		PDField field = acroForm.getField(fieldName);
		if (field != null) {
			if (value == null) {
				field.setValue("");
			} else {
				value = PdfDocument.cleanCharacters(value);
		    	try {
					field.setValue(value);
				} catch (IllegalArgumentException e) {
					log.warn("Cannot set PDF field value: {}", value, e);
					field.setValue(Normalizer.normalize(value, Normalizer.Form.NFKD)
							.replaceAll("\\p{InCombiningDiacriticalMarks}+",""));
				}
			}

			field.setReadOnly(true);
			field.setNoExport(true);
		}
	}
}