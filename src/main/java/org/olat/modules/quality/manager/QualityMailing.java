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
package org.olat.modules.quality.manager;

import static org.olat.modules.forms.EvaluationFormSurveyIdentifier.of;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormDispatcher;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormPrintSelection;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric.NameDisplay;
import org.olat.modules.forms.ui.EvaluationFormFormatter;
import org.olat.modules.forms.ui.EvaluationFormPrintControllerCreator;
import org.olat.modules.forms.ui.LegendNameGenerator;
import org.olat.modules.forms.ui.NameShuffleAnonymousComparator;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.SessionInformationLegendNameGenerator;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationSearchParams;
import org.olat.modules.quality.QualityModule;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.model.QualityMailTemplateBuilder;
import org.olat.modules.quality.ui.FiguresFactory;
import org.olat.modules.quality.ui.QualityMainController;
import org.olat.modules.quality.ui.QualityUIContextsBuilder;
import org.olat.modules.quality.ui.QualityUIContextsBuilder.Attribute;
import org.olat.modules.quality.ui.QualityUIContextsBuilder.KeyValue;
import org.olat.modules.quality.ui.QualityUIContextsBuilder.UIContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class QualityMailing {

	private static final Logger log = Tracing.createLoggerFor(QualityMailing.class);
	
	private static final SimpleDateFormat FILE_DATE_PREFIX = new SimpleDateFormat("yyyyMMdd");
	private static final Collection<QualityDataCollectionStatus> STATUS_FILTER = Arrays.asList(
			QualityDataCollectionStatus.READY,
			QualityDataCollectionStatus.RUNNING,
			QualityDataCollectionStatus.FINISHED);
	
	@Autowired
	private QualityModule qualityModule;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private QualityParticipationDAO participationDao;
	@Autowired
	private QualityDataCollectionDAO dataCollectionDao;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	@Autowired
	private I18nModule i18nModule;
	
	public void sendAnnouncementMail(QualityReminder reminder, Identity topicIdentity) {
		MailTemplate template = createAnnouncementMailTemplate(reminder, topicIdentity);
		
		MailerResult result = new MailerResult();
		MailManager mailManager = CoreSpringFactory.getImpl(MailManager.class);
		MailBundle bundle = mailManager.makeMailBundle(null, topicIdentity, template, null, null, result);
		if(bundle != null) {
			appendMimeFrom(bundle);
			result = mailManager.sendMessage(bundle);
			logMailerResult(result, reminder, topicIdentity);
		}
	}

	private MailTemplate createAnnouncementMailTemplate(QualityReminder reminder, Identity topicIdentity) {
		User user = topicIdentity.getUser();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(user.getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(QualityMainController.class, locale);
		
		String subject = translator.translate(reminder.getType().getSubjectI18nKey());
		String body = translator.translate(reminder.getType().getBodyI18nKey());
		QualityMailTemplateBuilder mailBuilder = QualityMailTemplateBuilder.builder(subject, body, locale);
		
		QualityDataCollection dataCollection = reminder.getDataCollection();
		mailBuilder.withStart(dataCollection.getStart())
				.withDeadline(dataCollection.getDeadline())
				.withTopicType(translator.translate(QualityDataCollectionTopicType.IDENTIY.getI18nKey()))
				.withTopic(user.getFirstName() + " " + user.getLastName())
				.withTitle(dataCollection.getTitle());
		
		EvaluationFormSurvey survey = qualityService.loadSurvey(dataCollection);
		EvaluationFormSurvey previous = survey.getSeriesPrevious();
		if (previous != null) {
			EvaluationFormSurveyIdentifier identifier = previous.getIdentifier();
			Long key = identifier.getOLATResourceable().getResourceableId();
			QualityDataCollection previousDC = qualityService.loadDataCollectionByKey(() -> key);
			if (previousDC != null) {
				mailBuilder.withPreviousTitle(previousDC.getTitle());
			}
		}
		
		String seriePorition = previous != null
				? translator.translate("reminder.serie.followup")
				: translator.translate("reminder.serie.primary");
		mailBuilder.withSeriePosition(seriePorition);
		
		String surveyContext = createDatCollectionContext(dataCollection, locale);
		mailBuilder.withContext(surveyContext);
		
		return mailBuilder.build();
	}

	void sendReminderMail(QualityReminder reminder, QualityReminder invitation, EvaluationFormParticipation participation) {
		if (participation.getExecutor() != null) {
			Identity executor = participation.getExecutor();
			MailTemplate template = createAnnouncementMailTemplate(reminder, invitation, executor);
			
			MailerResult result = new MailerResult();
			MailManager mailManager = CoreSpringFactory.getImpl(MailManager.class);
			MailBundle bundle = mailManager.makeMailBundle(null, executor, template, null, null, result);
			if(bundle != null) {
				appendMimeFrom(bundle);
				result = mailManager.sendMessage(bundle);
				logMailerResult(result, reminder, executor);
			}
		}
	}

	private void logMailerResult(MailerResult result, QualityReminder reminder, Identity executor) {
		if (result.isSuccessful()) {
			log.info(MessageFormat.format("{0} for quality data collection [key={1}] sent to {2}",
					reminder.getType().name(), reminder.getDataCollection().getKey(), executor));
		} else {
			log.warn(MessageFormat.format("Sending {0} for quality data collection [key={1}] to {2} failed: {3}",
					reminder.getType().name(), reminder.getDataCollection().getKey(), executor, result.getErrorMessage()));
		}
	}

	private MailTemplate createAnnouncementMailTemplate(QualityReminder reminder, QualityReminder invitationReminder, Identity executor) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(executor.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(QualityMainController.class, locale);
		
		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setExecutorRef(executor);
		searchParams.setDataCollectionRef(reminder.getDataCollection());
		searchParams.setDataCollectionStatus(STATUS_FILTER);
		List<QualityExecutorParticipation> participations = participationDao.loadExecutorParticipations(translator, searchParams , 0, -1);
		QualityExecutorParticipation participation = null;
		if (!participations.isEmpty()) {
			participation = participations.get(0);
		}
		
		String subject = translator.translate(reminder.getType().getSubjectI18nKey());
		String body = translator.translate(reminder.getType().getBodyI18nKey());
		QualityMailTemplateBuilder mailBuilder = QualityMailTemplateBuilder.builder(subject, body, locale);
		
		User user = executor.getUser();
		mailBuilder.withExecutor(user);
		
		if (participation != null) {
			mailBuilder.withStart(participation.getStart())
					.withDeadline(participation.getDeadline())
					.withTopic(participation.getTopic())
					.withTitle(participation.getTitle())
					.withPreviousTitle(participation.getPreviousTitle());
			
			String custom = translator.translate(QualityDataCollectionTopicType.CUSTOM.getI18nKey());
			if (!custom.equals(participation.getTranslatedTopicType())) {
				mailBuilder.withTopicType(participation.getTranslatedTopicType());
			}
			
			String seriePorition = participation.getPreviousTitle() != null
					? translator.translate("reminder.serie.followup")
					: translator.translate("reminder.serie.primary");
			mailBuilder.withSeriePosition(seriePorition);
			
			String url = EvaluationFormDispatcher.getExecutionUrl(participation.getParticipationIdentifier());
			mailBuilder.withUrl(url);
			
			String surveyContext = createParticipationContext(participation, locale);
			mailBuilder.withContext(surveyContext);
		}
	
		if (invitationReminder != null) {
			mailBuilder.withInvitation(invitationReminder.getSendDone());
		}
		
		return mailBuilder.build();
	}

	private String createParticipationContext(QualityExecutorParticipation participation, Locale locale) {
		StringBuilder sb = new StringBuilder();
		List<UIContext> uiContexts = QualityUIContextsBuilder.builder(participation, locale)
				.addAttribute(Attribute.ROLE)
				.addAttribute(Attribute.COURSE)
				.addAttribute(Attribute.CURRICULUM_ELEMENTS)
				.addAttribute(Attribute.TAXONOMY_LEVELS)
				.build()
				.getUiContexts();
		for (UIContext uiContext : uiContexts) {
			sb.append("<br/>");
			for (KeyValue kv : uiContext.getKeyValues()) {
				sb.append("<br/>").append(kv.getKey()).append(": ").append(kv.getValue());
			}
		}
		return sb.toString();
	}

	void sendReportAccessEmail(QualityDataCollection dataCollection, Collection<Identity> recipients,
			List<RubricStatistic> rubricStatistics) {
		Path tempDir = Paths.get(WebappHelper.getTmpDir()).resolve(CodeHelper.getUniqueID());
		try {
			Files.createDirectories(tempDir);
		} catch (Exception e) {
			log.error("Creation of temp directory for PDF report in quality e-mail failed! Path: " + tempDir, e);
		}
		
		for (Identity recipient : recipients) {
			sendReportAccessEmail(dataCollection, recipient, rubricStatistics, tempDir);
		}
		
		try {
			Files.delete(tempDir);
		} catch (IOException e) {
			// It is just a temp dir
		}
	}
	
	private void sendReportAccessEmail(QualityDataCollection dataCollection, Identity recipient,
			List<RubricStatistic> rubricStatistics, Path tempDir) {
		MailTemplate template = createReportAccessMailTemplate(dataCollection, recipient, rubricStatistics, tempDir);
		
		MailerResult result = new MailerResult();
		MailManager mailManager = CoreSpringFactory.getImpl(MailManager.class);
		MailBundle bundle = mailManager.makeMailBundle(null, recipient, template, null, null, result);
		if(bundle != null) {
			appendMimeFrom(bundle);
			result = mailManager.sendMessage(bundle);
			if (result.isSuccessful()) {
				log.info("Report access email send");
				log.info(MessageFormat.format("Report access email for quality data collection [key={0}] sent to {1}",
						dataCollection.getKey(), recipient));
			} else {
				log.warn(MessageFormat.format("Sending report access email for quality data collection [key={0}] to {1} failed: {2}",
						dataCollection.getKey(), recipient, result.getErrorMessage()));
			}
		}
	}

	private MailTemplate createReportAccessMailTemplate(QualityDataCollection dataCollection, Identity recipient,
			List<RubricStatistic> rubricStatistics, Path tempDir) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(recipient.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(QualityMainController.class, locale);
		
		String subject = translator.translate("report.access.email.subject");
		String body = translator.translate("report.access.email.body");
		QualityMailTemplateBuilder mailBuilder = QualityMailTemplateBuilder.builder(subject, body, locale);
		
		User user = recipient.getUser();
		mailBuilder.withExecutor(user);
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		searchParams.setDataCollectionRef(dataCollection);
		List<QualityDataCollectionView> dataCollectionViews = dataCollectionDao.loadDataCollections(translator, searchParams, 0, -1);
		
		QualityDataCollectionView dataCollectionView = null;
		if (!dataCollectionViews.isEmpty()) {
			dataCollectionView = dataCollectionViews.get(0);
			mailBuilder.withStart(dataCollectionView.getStart())
					.withDeadline(dataCollectionView.getDeadline())
					.withTopic(dataCollectionView.getTopic())
					.withTitle(dataCollectionView.getTitle())
					.withPreviousTitle(dataCollectionView.getPreviousTitle());
			
			String custom = translator.translate(QualityDataCollectionTopicType.CUSTOM.getI18nKey());
			if (!custom.equals(dataCollectionView.getTranslatedTopicType())) {
				mailBuilder.withTopicType(dataCollectionView.getTranslatedTopicType());
			}
			
			String seriePorition = dataCollectionView.getPreviousTitle() != null
					? translator.translate("reminder.serie.followup")
					: translator.translate("reminder.serie.primary");
			mailBuilder.withSeriePosition(seriePorition);
		}
		
		String url = getReportUrl(dataCollection.getKey());
		mailBuilder.withUrl(url);
		
		String surveyContext = createDatCollectionContext(dataCollection, locale);
		mailBuilder.withContext(surveyContext);
		
		String result = getResult(translator, rubricStatistics);
		mailBuilder.withResult(result);
		
		File reportPdf = getReportPdf(dataCollection, dataCollectionView, recipient, tempDir, locale);
		mailBuilder.withReportPfd(reportPdf);
		
		return mailBuilder.build();
	}
	
	private String getReportUrl(Long dataCollectionKey) {
		StringBuilder url = new StringBuilder();
		url.append(Settings.getServerContextPathURI());
		url.append("/url/QualitySite/0/quality/0/datacollections/0/");
		if (dataCollectionKey != null) {
			url.append("datacollection/");
			url.append(dataCollectionKey);
			url.append("/report/0/");
		}
		return url.toString();
	}
	
	private String createDatCollectionContext(QualityDataCollection dataCollection, Locale locale) {
		StringBuilder sb = new StringBuilder();
		List<UIContext> uiContexts = QualityUIContextsBuilder.builder(dataCollection, locale)
				.addAttribute(Attribute.ROLE)
				.addAttribute(Attribute.CURRICULUM_ELEMENTS)
				.addAttribute(Attribute.COURSE)
				.addAttribute(Attribute.TAXONOMY_LEVELS)
				.build()
				.getUiContexts();
		for (UIContext uiContext : uiContexts) {
			sb.append("<br/>");
			for (KeyValue kv : uiContext.getKeyValues()) {
				sb.append("<br/>").append(kv.getKey()).append(": ").append(kv.getValue());
			}
		}
		return sb.toString();
	}
	
	private String getResult(Translator translator, List<RubricStatistic> rubricStatistics) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < rubricStatistics.size(); i++) {
			RubricStatistic rubricStatistic = rubricStatistics.get(i);
			
			String index = String.valueOf(i + 1);
			String rubricName = rubricStatistic.getRubric().getName();
			String rubricTranslatedIndexName = 
					rubricStatistic.getRubric().getNameDisplays().contains(NameDisplay.report)
					&& StringHelper.containsNonWhitespace(rubricName)
							? translator.translate("report.access.email.rubric.index.name", new String[] {index, rubricName})
							: translator.translate("report.access.email.rubric.index", new String[] {index});
			RubricRating rating = rubricStatistic.getTotalStatistic().getRating();
			String translatedRating = translator.translate(getRatingI18n(rating));
			
			String[] args = {
					rubricTranslatedIndexName,                                // rubric index and name
					EvaluationFormFormatter.formatDouble(
							rubricStatistic.getTotalStatistic().getAvg()),    // average
					translatedRating                                          // rating
			};
			
			sb.append("<br/>");
			sb.append(translator.translate("report.access.email.rubric.rating", args));
		}
		return sb.toString();
	}

	private String getRatingI18n(RubricRating rating) {
		switch (rating) {
		case SUFFICIENT: 
			return "report.access.rating.sufficient";
		case NEUTRAL: 
			return "report.access.rating.neutral";
		case INSUFFICIENT: 
			return "report.access.rating.insufficient";
		default: 
			return "report.access.rating.not.rated";
		}
	}
	
	private File getReportPdf(QualityDataCollection dataCollection, QualityDataCollectionView dataCollectionView, Identity recipient, Path tempDir, Locale locale) {
		if (!pdfModule.isEnabled()) return null;
		if (tempDir == null || !tempDir.toFile().exists()) return null;
		
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(of(dataCollection));
		Form form = evaluationFormManager.loadForm(survey.getFormEntry());
		DataStorage storage = evaluationFormManager.loadStorage(survey.getFormEntry());
		SessionFilter filter = SessionFilterFactory.createSelectDone(survey);
		LegendNameGenerator legendNameGenerator = new SessionInformationLegendNameGenerator(filter);
		EvaluationFormPrintSelection printSelection = new EvaluationFormPrintSelection();
		printSelection.setOverview(true);
		printSelection.setTables(true);
		
		String localeKey = i18nModule.getLocaleKey(locale);
		File reportPdf = getPdfOverviewReport(tempDir, localeKey);
		if (reportPdf == null || !reportPdf.exists()) {
			reportPdf = tempDir.resolve(getFileName(dataCollection, localeKey)).toFile();
			reportPdf = createPdfOverviewReport(dataCollection, dataCollectionView, form, storage, filter,
					legendNameGenerator, printSelection, recipient, reportPdf, locale);
		}
		if (reportPdf.exists()) {
			//check if file has content
			if (reportPdf.length() > 0) {
				return reportPdf;
			}
			FileUtils.deleteFile(reportPdf);
		}
		return null;
	}

	private static File getPdfOverviewReport(Path tempDir, String localeKey) {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempDir, "*" + localeKey + ".pdf")) {
			for (Path path : directoryStream) {
				 // return first, it exists maximal one
				return path.toFile();
			}
		} catch (IOException e) {
			log.error("", e);
		}
		return null;
	}
	
	private static String getFileName(QualityDataCollection dataCollection, String localeKey) {
		Date now = new Date();
		StringBuilder sb = new StringBuilder();
		sb.append(FILE_DATE_PREFIX.format(now));
		sb.append("_");
		sb.append(FileUtils.normalizeFilename(dataCollection.getTitle()));
		sb.append("_");
		sb.append(localeKey);
		sb.append(".pdf");
		return sb.toString();
	}

	private File createPdfOverviewReport(QualityDataCollection dataCollection,
			QualityDataCollectionView dataCollectionView, Form form, DataStorage storage, SessionFilter filter,
			LegendNameGenerator legendNameGenerator, EvaluationFormPrintSelection printSelection, Identity recipient,
			File reportPdf, Locale locale) {
		ControllerCreator controllerCreator = getControllerCreator(dataCollection, dataCollectionView, form, storage,
				filter, legendNameGenerator, printSelection, locale);
		try (OutputStream out = new FileOutputStream(reportPdf)) {
			WindowControl bwControl = new WindowControlMocker();
			pdfService.convert(recipient, controllerCreator, bwControl, out);
			return reportPdf;
		} catch (IOException e) {
			log.error("Error while saving quality overview report! Path: " + reportPdf.getAbsolutePath(), e);
		}
		return null;
	}
	
	private ControllerCreator getControllerCreator(QualityDataCollection dataCollection,
			QualityDataCollectionView dataCollectionView, Form form, DataStorage storage, SessionFilter filter,
			LegendNameGenerator legendNameGenerator, EvaluationFormPrintSelection printSelection, Locale locale) {
		
		Figures figures = FiguresFactory.createOverviewFigures(dataCollection, dataCollectionView, locale);
		Comparator<EvaluationFormSession> comparator = new NameShuffleAnonymousComparator();
		ReportHelper reportHelper = ReportHelper.builder(locale).withLegendNameGenrator(legendNameGenerator)
				.withSessionComparator(comparator).withColors().build();

		return new EvaluationFormPrintControllerCreator(form, storage, filter, figures, reportHelper, printSelection,
				dataCollection.getTitle());
	}
	
	private void appendMimeFrom(MailBundle bundle) {
		if (StringHelper.containsNonWhitespace(qualityModule.getFromEmail())) {
			bundle.setMimeFromEmail(qualityModule.getFromEmail());
			bundle.setMimeFromName(qualityModule.getFromName());
		}
		
	}

}
