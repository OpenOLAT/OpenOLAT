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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 16.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatePhantomWorker {
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
	private final UserManager userManager;
	private final CertificatesManagerImpl certificatesManager;

	public CertificatePhantomWorker(Identity identity, RepositoryEntry entry, Float score, Float maxScore, Boolean passed,
			Double completion, Date dateCertification, Date dateFirstCertification, Date nextRecertificationDate, String custom1,
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
		this.dateNextRecertification = nextRecertificationDate;
		this.certificateURL = certificateURL;
		this.userManager = userManager;
		this.certificatesManager = certificatesManager;
	}

	public File fill(CertificateTemplate template, File destinationDir, String filename) {
		File certificateFile = new File(destinationDir, filename);
		File templateFile = certificatesManager.getTemplateFile(template);
		File htmlCertificateFile = copyAndEnrichTemplate(templateFile);

		List<String> cmds = new ArrayList<>();
		cmds.add("phantomjs");
		cmds.add(certificatesManager.getRasterizePath().toFile().getAbsolutePath());
		cmds.add(htmlCertificateFile.getAbsolutePath());
		cmds.add(certificateFile.getAbsolutePath());
		if(StringHelper.containsNonWhitespace(template.getFormat())) {
			cmds.add(template.getFormat());
		} else {
			cmds.add("A4");
		}
		if(StringHelper.containsNonWhitespace(template.getOrientation())) {
			cmds.add(template.getOrientation());
		} else {
			cmds.add("portrait");
		}
		
		CountDownLatch doneSignal = new CountDownLatch(1);
		ProcessWorker worker = new ProcessWorker(cmds, htmlCertificateFile, doneSignal);
		worker.start();

		try {
			if(!doneSignal.await(30000, TimeUnit.MILLISECONDS)) {
				log.warn("Cannot output certificates in 30s: {}", certificateFile);
			}
		} catch (InterruptedException e) {
			log.error("", e);
		}
		
		worker.destroyProcess();
		return certificateFile;
	}
	
	private File copyAndEnrichTemplate(File templateFile) {
		VelocityContext context = getContext();
		boolean result = false;
		File htmlCertificate = new File(templateFile.getParent(), "c" + UUID.randomUUID() + ".html");
		try(Reader in = Files.newBufferedReader(templateFile.toPath(), StandardCharsets.UTF_8);
			Writer output = new FileWriter(htmlCertificate)) {
			result = certificatesManager.getVelocityEngine().evaluate(context, output, "mailTemplate", in);
			output.flush();
		} catch(Exception e) {
			log.error("", e);
		}
		return result ? htmlCertificate : null;
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
	
	public static boolean checkPhantomJSAvailabilty() {
		List<String> cmds = new ArrayList<>();
		cmds.add("phantomjs");
		cmds.add("--help");
		
		CountDownLatch doneSignal = new CountDownLatch(1);
		ProcessWorker worker = new ProcessWorker(cmds, null, doneSignal);
		worker.start();

		try {
			if(!doneSignal.await(10000, TimeUnit.MILLISECONDS)) {
				log.warn("Cannot check PhantomJS availability in 30s.");
			}
		} catch (InterruptedException e) {
			log.error("", e);
		}
		
		log.info("PhantomJS help is available if exit value = 0: {}", worker.getExitValue());
		return worker.getExitValue() == 0;
	}
	
	public static class DateFormatter {

		public String formatDate(Date date, String language) {
			Locale locale = I18nManager.getInstance().getLocaleOrDefault(language);
			Formatter formatter = Formatter.getInstance(locale);
			return formatter.formatDate(date);
		}
		
		public String formatDateLong(Date date, String language) {
			Locale locale = I18nManager.getInstance().getLocaleOrDefault(language);
			Formatter formatter = Formatter.getInstance(locale);
			return formatter.formatDateLong(date);
		}
	}

	private static class ProcessWorker extends Thread {
		
		private volatile Process process;

		private int exitValue = -1;
		private final List<String> cmd;
		private final CountDownLatch doneSignal;
		private final File htmlCertificateFile;
		
		public ProcessWorker(List<String> cmd, File htmlCertificateFile, CountDownLatch doneSignal) {
			this.cmd = cmd;
			this.doneSignal = doneSignal;
			this.htmlCertificateFile = htmlCertificateFile;
		}
		
		public void destroyProcess() {
			if (process != null) {
				process.destroy();
				process = null;
			}
		}
		
		public int getExitValue() {
			return exitValue;
		}

		@Override
		public void run() {
			try {
				if(log.isDebugEnabled()) {
					log.debug(cmd.toString());
				}
				
				ProcessBuilder builder = new ProcessBuilder(cmd);
				process = builder.start();
				executeProcess(process);
				doneSignal.countDown();
			} catch (IOException e) {
				log.error ("Could not spawn convert sub process", e);
				destroyProcess();
			} finally {
				if(htmlCertificateFile != null) {
					FileUtils.deleteFile(htmlCertificateFile);
				}
			}
		}
		
		private final void executeProcess(Process proc) {
			StringBuilder errors = new StringBuilder();
			StringBuilder output = new StringBuilder();
			String line;

			InputStream stderr = proc.getErrorStream();
			InputStreamReader iserr = new InputStreamReader(stderr);
			BufferedReader berr = new BufferedReader(iserr);
			line = null;
			try {
				while ((line = berr.readLine()) != null) {
					errors.append(line);
				}
			} catch (IOException e) {
				//
			}
			
			InputStream stdout = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			BufferedReader br = new BufferedReader(isr);
			line = null;
			try {
				while ((line = br.readLine()) != null) {
					output.append(line);
				}
			} catch (IOException e) {
				//
			}
			
			if(log.isDebugEnabled()) {
				log.debug("Error: {}", errors.toString());
				log.debug("Output: {}", output.toString());
			}

			try {
				exitValue = proc.waitFor();
				if (exitValue != 0) {
					log.warn("Problem with PhantomJS? {}", exitValue);
				}
			} catch (InterruptedException e) {
				log.warn("Takes too long");
			}
		}
	}
}
