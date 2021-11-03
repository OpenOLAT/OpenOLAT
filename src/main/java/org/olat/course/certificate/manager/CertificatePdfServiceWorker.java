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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Worker which use the PDF service in OpenOLAT
 * to produce the certificates based on HTML templates.
 * 
 * 
 * Initial date: 8 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatePdfServiceWorker {
	private static final Logger log = Tracing
			.createLoggerFor(CertificatePDFFormWorker.class);
	
	private final Float score;
	private final Float maxScore;
	private final Boolean passed;
	private final Double completion;
	private final Identity identity;
	private final RepositoryEntry entry;
	private final String certificateURL;

	private final Date dateCertification;
	private final Date dateFirstCertification;
	private final Date dateNextRecertification;
	private final String custom1;
	private final String custom2;
	private final String custom3;

	private final Locale locale;
	private final PdfService pdfService;
	private final UserManager userManager;
	private final CertificatesManagerImpl certificatesManager;

	public CertificatePdfServiceWorker(Identity identity, RepositoryEntry entry, Float score, Float maxScore, Boolean passed,
			Double completion, Date dateCertification, Date dateFirstCertification, Date nextRecertificationDate, String custom1,
			String custom2, String custom3, String certificateURL, Locale locale, UserManager userManager,
			CertificatesManagerImpl certificatesManager, PdfService pdfService) {
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
		this.dateNextRecertification = nextRecertificationDate;
		this.certificateURL = certificateURL;
		this.pdfService = pdfService;
		this.userManager = userManager;
		this.certificatesManager = certificatesManager;
	}

	public File fill(CertificateTemplate template, File destinationDir, String filename) {
		File certificateFile = new File(destinationDir, filename);
		File templateFile = certificatesManager.getTemplateFile(template);
		File htmlCertificateFile = copyAndEnrichTemplate(templateFile);
		File qrCodeScriptFile = new File(htmlCertificateFile.getParent(), "qrcode.min.js");
		if(!qrCodeScriptFile.exists()) {
			try(InputStream inQRCodeLib = CertificatesManager.class.getResourceAsStream("qrcode.min.js")) {
				Files.copy(inQRCodeLib, qrCodeScriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);	
			} catch(Exception e) {
				log.error("Can not read qrcode.min.js for QR Code PDF generation", e);
			}
		}

		try(OutputStream out = new FileOutputStream(certificateFile)) {
			pdfService.convert(htmlCertificateFile.getParentFile(), htmlCertificateFile.getName(), out);
		} catch(Exception e) {
			log.error("", e);
			certificateFile = null;
		}
		
		try {
			Files.deleteIfExists(htmlCertificateFile.toPath());
		} catch (IOException e) {
			log.error("", e);
		}
		return certificateFile;
	}
	
	private File copyAndEnrichTemplate(File templateFile) {
		boolean result = false;
		File htmlCertificate = new File(templateFile.getParent(), "c" + UUID.randomUUID() + ".html");
		
		try(Reader in = Files.newBufferedReader(templateFile.toPath(), Charset.forName("UTF-8"));
				StringOutput content = new StringOutput(32000);
				Writer output = new FileWriter(htmlCertificate)) {
			VelocityContext context = getContext();
			result = certificatesManager.getVelocityEngine().evaluate(context, content, "mailTemplate", in);
			content.flush();
			
			if(hasQRCode(content)) {
				injectQRCodeScript(content);
			}
			
			output.write(content.toString());
			output.flush();
			result = true;
		} catch(Exception e) {
			log.error("", e);
		}
		return result ? htmlCertificate : null;
	}
	
	private boolean hasQRCode(StringOutput content) {
		return content.contains("o_qrcode");
	}
	
	private void injectQRCodeScript(StringOutput content) {
		int injectionIndex = injectionPoint(content);
		
		StringBuilder qr = new StringBuilder(512);
		qr.append("<script src='qrcode.min.js'></script>\n")
		  .append("<script>\n")
		  .append("/* <![CDATA[ */ \n")
		  .append("document.addEventListener('load', new function() {\n")
		  .append("  var qrcodes = document.querySelectorAll('.o_qrcode');\n")
		  .append("  for (var i=0; i<qrcodes.length; i++) {\n")
		  .append("    var qrcode = qrcodes[i];\n")
		  .append("    var val = qrcode.textContent;\n")
		  .append("    while (qrcode.firstChild) {\n")
		  .append("      qrcode.removeChild(qrcode.firstChild);\n")
		  .append("    }\n")
		  .append("    new QRCode(qrcode, val);\n")
		  .append("  }\n")
		  .append("});\n")
		  .append("/* ]]> */\n")
		  .append("</script>");
		content.insert(injectionIndex, qr.toString());	
	}
	
	private int injectionPoint(StringOutput content) {
		String[] anchors = new String[] { "</body", "</ body", "</BODY", "</ BODY", "</html", "</HTML" };
		for(String anchor:anchors) {
			int bodyIndex = content.indexOf(anchor);
			if(bodyIndex > 0) {
				return bodyIndex;
			}
		}
		return content.length();// last hope
	}
	
	private VelocityContext getContext() {
		VelocityContext context = new VelocityContext();
		fillUserProperties(context);
		fillRepositoryEntry(context);
		fillCertificationInfos(context);
		fillAssessmentInfos(context);
		fillMetaInfos(context);
		context.put("formatter", new DateFormatter());
		return context;
	}
	
	private void fillUserProperties(VelocityContext context) {
		User user = identity.getUser();
		List<UserPropertyHandler> userPropertyHandlers = userManager.getAllUserPropertyHandlers();
		for (UserPropertyHandler handler : userPropertyHandlers) {
			String propertyName = handler.getName();
			String value = handler.getUserProperty(user, locale);
			context.put(propertyName, value);
		}
		
		String fullName = userManager.getUserDisplayName(identity);
		context.put("fullName", fullName);
		
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
		context.put("firstNameLastName", firstNameLastName.toString());
		context.put("lastNameFirstName", lastNameFirstName.toString());
	}
	
	private void fillRepositoryEntry(VelocityContext context) {
		String title = entry.getDisplayname();
		context.put("title", title);
		String description = entry.getDescription();
		context.put("description", description);
		String requirements = entry.getRequirements();
		context.put("requirements", requirements);
		String objectives = entry.getObjectives();
		context.put("objectives", objectives);
		String credits = entry.getCredits();
		context.put("credits", credits);
		String externalRef = entry.getExternalRef();
		context.put("externalReference", externalRef);
		String authors = entry.getAuthors();
		context.put("authors", authors);
		String expenditureOfWorks = entry.getExpenditureOfWork();
		context.put("expenditureOfWorks", expenditureOfWorks);
		String mainLanguage = entry.getMainLanguage();
		context.put("mainLanguage", mainLanguage);
		String location = entry.getLocation();
		context.put("location", location);
		
		if (entry.getLifecycle() != null) {
			Formatter format = Formatter.getInstance(locale);

			Date from = entry.getLifecycle().getValidFrom();
			String formattedFrom = format.formatDate(from);
			context.put("from", formattedFrom);
			String formattedFromLong = format.formatDateLong(from);
			context.put("fromLong", formattedFromLong);

			Date to = entry.getLifecycle().getValidTo();
			String formattedTo = format.formatDate(to);
			context.put("to", formattedTo);
			String formattedToLong = format.formatDateLong(to);
			context.put("toLong", formattedToLong);
		}
	}
	
	private void fillCertificationInfos(VelocityContext context) {
		Formatter format = Formatter.getInstance(locale);
		context.put("dateFormatter", format);

		if(dateCertification == null) {
			context.put("dateCertification", "");
		} else {
			String formattedDateCertification = format.formatDate(dateCertification);
			context.put("dateCertification", formattedDateCertification);
			String formattedDateCertificationLong = format.formatDateLong(dateCertification);
			context.put("dateCertificationLong", formattedDateCertificationLong);
			context.put("dateCertificationRaw", dateCertification);
		}
		
		if(dateFirstCertification == null) {
			context.put("dateFirstCertification", "");
		} else {
			String formattedDateFirstCertification = format.formatDate(dateFirstCertification);
			context.put("dateFirstCertification", formattedDateFirstCertification);
			String formattedDateFirstCertificationLong = format.formatDate(dateFirstCertification);
			context.put("dateFirstCertificationLong", formattedDateFirstCertificationLong);
			context.put("dateFirstCertificationRaw", dateFirstCertification);
		}

		if(dateNextRecertification == null) {
			context.put("dateNextRecertification", "");
		} else {
			String formattedDateNextRecertification = format.formatDate(dateNextRecertification);
			context.put("dateNextRecertification", formattedDateNextRecertification);
			String formattedDateNextRecertificationLong = format.formatDateLong(dateNextRecertification);
			context.put("dateNextRecertificationLong", formattedDateNextRecertificationLong);
			context.put("dateNextRecertificationRaw", dateNextRecertification);
		}		
	}
	
	private void fillAssessmentInfos(VelocityContext context) {
		String roundedScore = AssessmentHelper.getRoundedScore(score);
		context.put("score", roundedScore);
		
		String roundedMaxScore = AssessmentHelper.getRoundedScore(maxScore);
		context.put("maxScore", roundedMaxScore);

		String status = (passed != null && passed.booleanValue()) ? "Passed" : "Failed";
		context.put("status", status);
		
		String roundedCompletion = completion != null? Formatter.roundToString(completion * 100, 0): null;
		context.put("progress", roundedCompletion);
	}
	
	private void fillMetaInfos(VelocityContext context) {
		context.put("custom1", custom1);
		context.put("custom2", custom2);
		context.put("custom3", custom3);
		context.put("certificateVerificationUrl", certificateURL);
	}
	
	public static class DateFormatter {

		public String formatDate(Date date, String language) {
			return formatDateInternal(date, language, 0, 0, 0, false);
		}
		public String formatDateRelative(Date date, String language, int days, int months, int years) {
			return formatDateInternal(date, language, days, months, years, false);
		}		
		public String formatDateLong(Date date, String language) {
			return formatDateInternal(date, language, 0, 0, 0, true);
		}
		public String formatDateLongRelative(Date date, String language, int days, int months, int years) {
			return formatDateInternal(date, language, days, months, years, true);
		}		
		
		private String formatDateInternal(Date baseLineDate, String language, int days, int months, int years, boolean longFormat) {
			if (baseLineDate == null) return null;
			Date date = baseLineDate;
			if (days != 0 || months != 0 || years != 0) {
				LocalDate localDate = LocalDateTime.ofInstant(baseLineDate.toInstant(),ZoneId.systemDefault()).toLocalDate();
				Period period = Period.of(years, months, days);
				LocalDate relativeDate = localDate.plus(period);
				date = Date.from(relativeDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());				
			}
			
			Locale locale = I18nManager.getInstance().getLocaleOrDefault(language);
			Formatter formatter = Formatter.getInstance(locale);			
			
			if (longFormat) {
				return formatter.formatDateLong(date);				
			} else {
				return formatter.formatDate(date);
			}
		}
	}

}
