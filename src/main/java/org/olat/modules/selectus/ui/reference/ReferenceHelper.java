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
package org.olat.modules.selectus.ui.reference;

import static org.olat.modules.selectus.manager.ApplicationMailTemplate.DEFAULT_TEMPLATE;
import static org.olat.modules.selectus.ui.RecruitingMailTemplate.APPLICATION_DEAR_TITLE_NAME;
import static org.olat.modules.selectus.ui.RecruitingMailTemplate.APPLICATION_FIRST_NAME;
import static org.olat.modules.selectus.ui.RecruitingMailTemplate.APPLICATION_LAST_NAME;
import static org.olat.modules.selectus.ui.RecruitingMailTemplate.HEAD_FIRST_NAME;
import static org.olat.modules.selectus.ui.RecruitingMailTemplate.HEAD_LAST_NAME;
import static org.olat.modules.selectus.ui.RecruitingMailTemplate.POSITION_MAIL;
import static org.olat.modules.selectus.ui.RecruitingMailTemplate.POSITION_TITLE;
import static org.olat.modules.selectus.ui.RecruitingMailTemplate.REFEREE_DEAR_TITLE_NAME;
import static org.olat.modules.selectus.ui.RecruitingMailTemplate.REFEREE_TITLE_LAST_NAME;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.AcademicalBackground;
import org.olat.modules.selectus.model.Address;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAttribute;
import org.olat.modules.selectus.model.ApplicationAttributeImpl;
import org.olat.modules.selectus.model.ApplicationImpl;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.AttachmentImpl;
import org.olat.modules.selectus.model.BusinessInformations;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.PersonGender;
import org.olat.modules.selectus.model.PersonImpl;
import org.olat.modules.selectus.model.PersonTitle;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.Project;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.attributes.SelectConfiguration;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.model.references.ReferenceImpl;
import org.olat.modules.selectus.ui.PositionApplicationsController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.mail.PositionMailTemplateRow.Type;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * 
 * Initial date: 20.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceHelper {
	
	private static final Logger log = Tracing.createLoggerFor(ReferenceHelper.class);
	private static final Pattern urlPattern = Pattern.compile("((mailto\\:|(news|(ht|f)tp(s?))\\://|www\\.)[-A-Za-z0-9+&@#/%?=~_|!:,\\.;]+[-A-Za-z0-9+&@#/%=~_|]*)");
	
	private ReferenceHelper() {
		//
	}
	
	public static Reference generateDummyReference() {
		ReferenceImpl ref = new ReferenceImpl();
		ref.setFirstName("Louis");
		ref.setLastName("de Broglie");
		ref.setInstitution("University of Paris");
		ref.setReferenceStatus(ReferenceStatus.submitted);
		ref.setReferenceType(ReferenceType.expert);
		ref.setEmail("louis.de.broglie@frentix.com");
		ref.setTitle("Dr.");
		ref.setSubmissionUrl(UUID.randomUUID().toString().toLowerCase());
		return ref;
	}
	
	public static List<Reference> generateDummyReferences() {
		List<Reference> refs = new ArrayList<>();
		refs.add(generateDummyReference("Louis", "de Broglie", ReferenceType.expert));
		refs.add(generateDummyReference("Marie", "Curie", ReferenceType.recommendation));
		refs.add(generateDummyReference("Paul", "Dirac", ReferenceType.recommendation));
		refs.add(generateDummyReference("Max", "Planck", ReferenceType.comparativeAssessmentExpert));
		return refs;
	}
	
	public static Reference generateDummyReference(String firstName, String lastName, ReferenceType type) {
		ReferenceImpl ref = new ReferenceImpl();
		ref.setFirstName(firstName);
		ref.setLastName(lastName);
		ref.setInstitution("University of Paris");
		ref.setReferenceStatus(ReferenceStatus.submitted);
		ref.setReferenceType(type);
		ref.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@frentix.com");
		ref.setTitle("Dr.");
		ref.setSubmissionUrl(UUID.randomUUID().toString().toLowerCase());
		
		AttachmentImpl attachment = new AttachmentImpl();
		attachment.setSize(33500);
		attachment.setName(firstName.toLowerCase() + "_" + lastName.toLowerCase() + ".pdf");
		attachment.setType("pdf");
		ref.setLetter(attachment);
		
		return ref;
	}
	
	/**
	 * Generate an application with dummy data. Only the
	 * data corresponding to the list of variables for
	 * emails are filled.
	 * 
	 * @param position The position for this application
	 * @return An application with dummy data
	 */
	public static Application generateDummyApplication(Position position) {
		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		Translator translator = Util.createPackageTranslator(PositionApplicationsController.class, Locale.ENGLISH);
		
		ApplicationImpl app = new ApplicationImpl();
		app.setCreationDate(new Date());
		app.setLastModified(app.getCreationDate());
		app.setPosition(position);
		app.setId(Integer.valueOf(42));
		app.setApplicantUrl(UUID.randomUUID().toString());
		
		Person person = new PersonImpl();
		person.setFirstName("Jane");
		person.setLastName("Smith");
		PersonTitle[] personTitles = recruitingModule.getApplicantPersonTitles();
		if(personTitles != null && personTitles.length > 0) {
			person.setTitle(personTitles[personTitles.length - 1].title());
		}
		person.setMail("jane.smith@selectus.ch");
		person.setGender(PersonGender.female.gender());
		app.setPerson(person);
		
		BusinessInformations businessInformations = app.getBusinessInformations();
		businessInformations.setOrganization(translator.translate("preview.person.businessinfos.organisation"));
		app.setBusinessInformations(businessInformations);

		generateDummyApplicationExtended(app);
		
		return app;
	}
	
	public static Application generateDummyApplicationExtended(Position position) {
		ApplicationImpl app =  (ApplicationImpl)generateDummyApplication(position);
		
		Person person = app.getPerson();
		person.setAcademicTitle("Prof.Dr.");
		person.setAdditionalNationalities("USA, Japan");
		person.setBirthday(generateDate(1987, 5, 25));
		person.setDisability(Boolean.TRUE);
		person.setNationality("Switzerland");
		person.setPhone("+41123456789");
		person.setMobilePhone("+417987654321");
		
		Address businessAddress = app.getBusinessAddress();
		businessAddress.setAddressLine1("Okenstr. 6");
		businessAddress.setAddressLine2("Address line 2");
		businessAddress.setAddressLine3("Address line 3");
		businessAddress.setZipCode("8500");
		businessAddress.setCity("Zurich");
		businessAddress.setCountry("CH");
		
		Address address = app.getAddress();
		address.setAddressLine1("Guterstr. 26");
		address.setAddressLine2("Address line 2");
		address.setAddressLine3("Address line 3");
		address.setZipCode("4500");
		address.setCity("Basel");
		address.setCountry("CH");

		BusinessInformations businessInformations = app.getBusinessInformations();
		businessInformations.setCurrentPosition("Senior Developer");
		businessInformations.setUnit("IT");
		
		AcademicalBackground background = app.getAcademicalBackground();
		background.setCareerDescription("Development of C++ applications");
		background.setCitations(5);
		background.setDissertationDate(generateDate(2005, 9, 1));
		background.setDissertationInstitution("Uni.");
		background.setDissertationKeyword1("Dissertation key 1");
		background.setDissertationKeyword2("Dissertation key 2");
		background.setDissertationKeyword3("Dissertation key 3");
		background.setDissertationTitle("Algorithm and data structures");
		background.setHabilitationDate(generateDate(2007, 10, 8));
		background.setHabilitationInstitution("Tech.");
		background.setHabilitationTitle("Recursive algorithms on non-linear systems");
		background.setHFactor(0.95d);
		background.setHighestDegreeDate(generateDate(2009, 11, 8));
		background.setHighestDegreeDescription("High degree of freedom");
		background.setHighestDegreeType(HighestDegreeType.prof.name());
		background.setNumberOfFirstAuthorships(3);
		background.setNumberOfLastAuthorships(4);
		background.setNumberOfOriginalPublications(2);
		background.setOrcid("AC-234");
		background.setWorkedInAcademiaSince("12");
		background.setWorkedOutAcademiaCareSince("2");
		background.setWorkedOutAcademiaSince("3");
		
		generateDummyApplicationExtended(app);
		
		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions();
		Set<ApplicationAttribute> attributes = new HashSet<>();
		for(PositionAttributeDefinition definition:definitions) {
			ApplicationAttributeImpl attr = new ApplicationAttributeImpl();
			attr.setApplication(app);
			attr.setDefinition(definition);
			
			if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.question) {
				attr.setValue("Custom text");
			} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.number) {
				attr.setValue("120001");
			} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.percentage) {
				attr.setValue("89");
			} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.date) {
				attr.setValue(Formatter.formatDatetime(new Date()));
			} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.select) {
				SelectConfiguration config = definition.getConfiguration(SelectConfiguration.class);
				if(config != null && config.getOptions() != null && !config.getOptions().isEmpty()) {
					String val = config.getOptions().get(0).getValue();
					if(!StringHelper.containsNonWhitespace(val)) {
						val = config.getOptions().get(0).getValueDe();
					}
					attr.setValue(val);
				}
			}
			attributes.add(attr);
		}
		app.setAttributes(attributes);
		
		for(DocumentEnum doc:DocumentEnum.values()) {
			AttachmentImpl attachment = new AttachmentImpl();
			attachment.setSize(2300);
			attachment.setName(doc.name() + ".pdf");
			attachment.setType("pdf");
			doc.setPath(app, attachment);
		}

		return app;
	}
	

	private static void generateDummyApplicationExtended(ApplicationImpl app) {
		Project project = app.getProject();
		project.setTitle("Quantum algorithms");
		project.setAcronym("QA");
		project.setDescription("Implement a quantum algorithm");
		project.setDisciplines("Quantum mechanics, informatics");
		project.setDuration("5 years+");
		project.setFinancialImpact1("10000");
		project.setFinancialImpact2("8000");
		project.setFinancialImpact3("6000");
		project.setFinancialImpact4("4000");
		project.setFinancialImpact5("1000");
		project.setKeywords("Physic, quantum computer, algorithm");
		project.setStartDate(generateDate(2022, 1, 2));
	}
	
	private static final Date generateDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, day);
		return cal.getTime();
	}
	
	public static Identity generateDummySecretary() {
		TransientIdentity id = new TransientIdentity();
		id.setProperty(UserConstants.FIRSTNAME, "Alfred");
		id.setProperty(UserConstants.LASTNAME, "Dupont");
		id.setProperty(UserConstants.TITLE, "Dr.");
		id.setProperty(UserConstants.TELMOBILE, "0787654321");
		id.setProperty(UserConstants.TELOFFICE, "0047654321");
		id.setProperty(UserConstants.TELPRIVATE, "0057654321");
		id.setProperty(UserConstants.EMAIL, "alfred.dupont@selectus.com");
		return id;
	}
	
	public static Identity generateDummyHeadOfCommittee() {
		TransientIdentity id = new TransientIdentity();
		id.setProperty(UserConstants.FIRSTNAME, "Marie");
		id.setProperty(UserConstants.LASTNAME, "Dupond");
		id.setProperty(UserConstants.TITLE, "Dr.");
		id.setProperty(UserConstants.TELMOBILE, "0797654321");
		id.setProperty(UserConstants.TELOFFICE, "0067654321");
		id.setProperty(UserConstants.TELPRIVATE, "0077654321");
		id.setProperty(UserConstants.EMAIL, "marie.dupond@selectus.com");
		return id;
	}
	
	public static String[] generateMailArguments(Identity headOfCommittee, Position position, Application application, Reference reference,
			SalutationGenerator salutationGenerator, Translator translator) {
		
		OrganisationUnit organisationSettings = CoreSpringFactory.getImpl(RecruitingService.class).getOrganisationUnit(position);
		String staffMail = CoreSpringFactory.getImpl(RecruitingModule.class).getStaffMail(position, organisationSettings);
		String refereeUrl = reference == null ? null : reference.getSubmissionUrl();
		Locale locale = translator.getLocale();

		String headLastname = "";
		String headFirstname = "";
		if(headOfCommittee != null ) {
			headLastname = headOfCommittee.getUser().getProperty(UserConstants.LASTNAME, locale);
			headFirstname = headOfCommittee.getUser().getProperty(UserConstants.FIRSTNAME, locale);
		}
		
		String dearTitleAndName = salutationGenerator.getSalutation(reference, locale);
		String titleLastName = salutationGenerator.getTitleLastName(reference, locale);
		
		String dearApplicatantTitleAndName = "";
		String applicantTitleLastName = "";
		String applicationTitleFullname = "";
		String applicationFirstName = "";
		String applicationLastName = "";
		if(application != null) {
			dearApplicatantTitleAndName = salutationGenerator.getSalutation(application, locale);
			applicantTitleLastName = salutationGenerator.getTitleLastName(application, locale);
			applicationTitleFullname = salutationGenerator.getTitleFullname(application, locale);
			applicationFirstName = application.getPerson().getFirstName();
			applicationLastName = application.getPerson().getLastName();
		}

		return new String[]{
				position.getMLTitle(locale), 	// 0
				staffMail,						// 1
				headLastname,					// 2
				headFirstname,					// 3
				refereeUrl,						// 4
				dearTitleAndName,				// 5
				titleLastName,					// 6
				dearApplicatantTitleAndName,	// 7
				applicantTitleLastName,			// 8
				applicationTitleFullname,		// 9
				applicationFirstName,			// 10
				applicationLastName				// 11
		};
	}
	
	public static final String[] getMailVariables() {
		return new String[]{
				"$" + POSITION_TITLE, 				// 0
				"$" + POSITION_MAIL,				// 1
				"$" + HEAD_LAST_NAME,				// 2
				"$" + HEAD_FIRST_NAME,				// 3
				"$" + "refereeUrl",					// 4
				"$" + REFEREE_DEAR_TITLE_NAME,		// 5
				"$" + REFEREE_TITLE_LAST_NAME,		// 6
				"$" + APPLICATION_DEAR_TITLE_NAME,	// 7
				"$" + "applicantTitleLastname",		// 8
				"$" + "applicantTitleFullName",		// 9
				"$" + APPLICATION_FIRST_NAME,		// 10
				"$" + APPLICATION_LAST_NAME			// 11
		};
	}
	
	public static final String[] getConfirmationSubmissionMailVariables(Type type) {
		String tfn = "refereeTitleFullname";
		if(type == Type.confirmationSubmissionExpert || type == Type.confirmationSubmissionComparativeExpert) {
			tfn = "expertTitleFullName";
		}
		return new String[]{
				"$" + tfn,		// 0
				"$" + "applicantFullname",			// 1
				"$" + "applicantList",				// 2
				"$" + POSITION_TITLE,				// 3
		};
	}
	
	public static SubjectAndBody referenceTemplateBase(Identity headOfCommittee, Position position, Application application, Reference reference,
			ReferenceType referenceType, SalutationGenerator salutationGenerator, Translator translator) {

		MailService mailService = CoreSpringFactory.getImpl(MailService.class);
		String[] args = generateMailArguments(headOfCommittee, position, application, reference, salutationGenerator, translator);

		if(referenceType == null && reference != null) {
			referenceType = reference.getReferenceType();
		}

		String body = null;
		String subject = null;
		MailAttachment letter = null;
		
		if(referenceType == ReferenceType.expert) {
			subject = position.getExpertRecommandationMailSubject();
			body = position.getExpertRecommandationMailTemplate();
			letter = mailService.toAttachment(position.getExpertRecommandationMailLetter(), application, translator.getLocale());
		} else if(referenceType == ReferenceType.recommendation) {
			subject = position.getRefereeRecommandationMailSubject();
			body = position.getRefereeRecommandationMailTemplate();
			letter = mailService.toAttachment(position.getRefereeRecommandationMailLetter(), application, translator.getLocale());
		} else if(referenceType == ReferenceType.comparativeAssessmentExpert) {
			subject = position.getComparativeAssessmentExpertMailSubject();
			body = position.getComparativeAssessmentExpertMailTemplate();
			letter = mailService.toAttachment(position.getComparativeAssessmentExpertMailLetter(), application, translator.getLocale());
		}
		
		if(!RecruitingHelper.containsTemplate(body)) {
			if(referenceType == ReferenceType.expert) {
				body = translator.translate("reference.expert.mail.body", args);
			} else if(referenceType == ReferenceType.recommendation) {
				body = translator.translate("reference.recommendation.mail.body", args);
			} else if(referenceType == ReferenceType.comparativeAssessmentExpert) {
				body = translator.translate("reference.comparative.expert.mail.body");
			}
		}

		if(!RecruitingHelper.containsTemplate(subject)) {
			if(referenceType == ReferenceType.expert) {
				subject = translator.translate("reference.expert.mail.subject", args);
			} else if(referenceType == ReferenceType.recommendation) {
				subject = translator.translate("reference.recommendation.mail.subject", args);
			} else if(referenceType == ReferenceType.comparativeAssessmentExpert) {
				subject = translator.translate("reference.comparative.expert.mail.subject", args);
			}
		}

		return new SubjectAndBody(subject, body, letter);
	}
	
	public static RecruitingMailTemplate referenceTemplate(Identity headOfCommittee, Identity secretary, Position position,
			Application app, List<? extends ApplicationShort> appList, Reference reference,
			SalutationGenerator salutationGenerator, Translator translator) {
		RecruitingService erFrontendManager = CoreSpringFactory.getImpl(RecruitingService.class);
		SubjectAndBody subjectAndBody = referenceTemplateBase(headOfCommittee, position, app,
				reference, reference.getReferenceType(), salutationGenerator, translator);

		RecruitingMailTemplate template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
				subjectAndBody.getSubject(), subjectAndBody.getBody(), subjectAndBody.getLetter(),
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
		
		MailerResult mailerResult = new MailerResult(); 
		SubjectAndBody subjectAndBody2 = erFrontendManager.createMailSender()
				.createWithContext(app, appList, reference, null, null, null, position, template, mailerResult);
		if(mailerResult.isSuccessful()) {
			template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
					subjectAndBody2.getSubject(), subjectAndBody2.getBody(), subjectAndBody2.getLetter(),
					headOfCommittee, secretary, subjectAndBody2, salutationGenerator, translator);
		}
		return template;
	}
	
	public static SubjectAndBody referenceReminderTemplateBase(Identity headOfCommittee, Position position,
			Application application, Reference reference,
			SalutationGenerator salutationGenerator, Translator translator) {
		
		SubjectAndBody referenceBase = referenceTemplateBase(headOfCommittee, position, application,
				reference, reference.getReferenceType(), salutationGenerator, translator);
		
		String subject = referenceBase.getSubject();
		String body = referenceBase.getBody();
		if(reference.getReferenceType() == ReferenceType.expert) {
			subject = translator.translate("reference.reminder.expert.mail.subject", subject);
			body = translator.translate("reference.reminder.expert.mail.body", subject);
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			subject = translator.translate("reference.reminder.recommendation.mail.subject", subject);
			body = translator.translate("reference.reminder.recommendation.mail.body", body);
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			subject = translator.translate("reference.reminder.comparative.expert.mail.subject", subject);
			body = translator.translate("reference.reminder.comparative.expert.mail.body", body);
		}
		
		return new SubjectAndBody(subject, body);
	}

	public static RecruitingMailTemplate referenceReminderTemplate(Identity headOfCommittee, Identity secretary, Position position,
			Application app, List<Application> appsList, Reference reference,
			SalutationGenerator salutationGenerator, Translator translator) {
		RecruitingService recruitingService = CoreSpringFactory.getImpl(RecruitingService.class);
		SubjectAndBody subjectAndBody = referenceReminderTemplateBase(headOfCommittee, position, app,
				reference, salutationGenerator, translator);

		RecruitingMailTemplate template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
				subjectAndBody.getSubject(), subjectAndBody.getBody(), null,
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
		
		MailerResult mailerResult = new MailerResult(); 
		SubjectAndBody subjectAndBody2 = recruitingService.createMailSender()
				.createWithContext(app, appsList, reference, null, null, null, position, template, mailerResult);
		if(mailerResult.isSuccessful()) {
			template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
					subjectAndBody2.getSubject(), subjectAndBody2.getBody(), null,
					headOfCommittee, secretary, subjectAndBody2, salutationGenerator, translator);
		}
		return template;
	}
	
	
	public static SubjectAndBody referenceReactivationTemplateBase(Identity headOfCommittee, Position position, Application application, Reference reference,
			SalutationGenerator salutationGenerator, Translator translator) {
		
		SubjectAndBody referenceBase = referenceTemplateBase(headOfCommittee, position, application,
				reference, reference.getReferenceType(), salutationGenerator, translator);
		
		String subject = referenceBase.getSubject();
		String body = referenceBase.getBody();
		if(reference.getReferenceType() == ReferenceType.expert) {
			subject = translator.translate("reference.reactivation.expert.mail.subject", subject);
			body = translator.translate("reference.reactivation.expert.mail.body", subject);
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			subject = translator.translate("reference.reactivation.recommendation.mail.subject", subject);
			body = translator.translate("reference.reactivation.recommendation.mail.body", body);
		}
		
		return new SubjectAndBody(subject, body);
	}

	public static RecruitingMailTemplate referenceReactivationTemplate(Identity headOfCommittee, Identity secretary, Position position, Application app, Reference reference,
			SalutationGenerator salutationGenerator, Translator translator) {
		RecruitingService erFrontendManager = CoreSpringFactory.getImpl(RecruitingService.class);
		SubjectAndBody subjectAndBody = referenceReactivationTemplateBase(headOfCommittee, position, app,
				reference, salutationGenerator, translator);

		RecruitingMailTemplate template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
				subjectAndBody.getSubject(), subjectAndBody.getBody(), null,
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
		
		MailerResult mailerResult = new MailerResult(); 
		SubjectAndBody subjectAndBody2 = erFrontendManager.createMailSender()
				.createWithContext(app, null, reference, null, null, null, position, template, mailerResult);
		if(mailerResult.isSuccessful()) {
			template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
					subjectAndBody2.getSubject(), subjectAndBody2.getBody(), null,
					headOfCommittee, secretary, subjectAndBody2, salutationGenerator, translator);
		}
		return template;
	}
	
	public static SubjectAndBody referenceDeactivationTemplateBase(Identity headOfCommittee, Position position, Application application, Reference reference,
			SalutationGenerator salutationGenerator, Translator translator) {
		
		String[] args = generateMailArguments(headOfCommittee, position, application, reference, salutationGenerator, translator);

		String body = null;
		String subject = null;
		if(reference.getReferenceType() == ReferenceType.expert) {
			subject = translator.translate("reference.deactivation.expert.mail.subject", args);
			body = translator.translate("reference.deactivation.expert.mail.body", args);
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			subject = translator.translate("reference.deactivation.recommendation.mail.subject", args);
			body = translator.translate("reference.deactivation.recommendation.mail.body", args);
		}
	
		return new SubjectAndBody(subject, body);
	}
	
	public static RecruitingMailTemplate referenceDeactivationTemplate(Identity headOfCommittee, Identity secretary, Position position, Application app, Reference reference,
			SalutationGenerator salutationGenerator, Translator translator) {
		RecruitingService erFrontendManager = CoreSpringFactory.getImpl(RecruitingService.class);
		SubjectAndBody subjectAndBody = referenceDeactivationTemplateBase(headOfCommittee, position, app,
				reference, salutationGenerator, translator);

		RecruitingMailTemplate template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
				subjectAndBody.getSubject(), subjectAndBody.getBody(), null,
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
		
		MailerResult mailerResult = new MailerResult(); 
		SubjectAndBody subjectAndBody2 = erFrontendManager.createMailSender()
				.createWithContext(app, null, reference, null, null, null, position, template, mailerResult);
		if(mailerResult.isSuccessful()) {
			template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
					subjectAndBody2.getSubject(), subjectAndBody2.getBody(), null,
					headOfCommittee, secretary, subjectAndBody2, salutationGenerator, translator);
		}
		return template;
	}
	
	
	public static SubjectAndBody assignmentTemplateBase(Identity headOfCommittee, Position position,
			SalutationGenerator salutationGenerator, Translator translator) {
		String[] args = generateMailArguments(headOfCommittee, position, null, null, salutationGenerator, translator);
		String subject = translator.translate("assignments.mail.subject", args);
		String body = translator.translate("assignments.mail.body", args);
		return new SubjectAndBody(subject, body, null);
	}
	
	public static RecruitingMailTemplate assignmentTemplate(Identity headOfCommittee, Identity secretary, Position position,
			SalutationGenerator salutationGenerator, Translator translator) {
		RecruitingService erFrontendManager = CoreSpringFactory.getImpl(RecruitingService.class);
		SubjectAndBody subjectAndBody = assignmentTemplateBase(headOfCommittee, position, salutationGenerator, translator);
		RecruitingMailTemplate template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
				subjectAndBody.getSubject(), subjectAndBody.getBody(), subjectAndBody.getLetter(),
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
		
		MailerResult mailerResult = new MailerResult(); 
		SubjectAndBody subjectAndBody2 = erFrontendManager.createMailSender()
				.createWithContext(null, null, null, null, null, null, position, template, mailerResult);
		if(mailerResult.isSuccessful()) {
			template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
					subjectAndBody2.getSubject(), subjectAndBody2.getBody(), subjectAndBody2.getLetter(),
					headOfCommittee, secretary, subjectAndBody2, salutationGenerator, translator);
		}
		return template;
	}
	
	public static boolean validateLinks(TextElement bodyEl, Reference reference) {
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			LinksCollector contentHandler = new LinksCollector();
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(bodyEl.getValue())));
			
			List<String> urls = contentHandler.getUrls();
			for(String url:urls) {
				if(!validateReferenceUrl(bodyEl, url, reference)) {
					return false;
				}
			}
			
			String content = contentHandler.getContent();
			return validateContent(bodyEl, content, reference);	
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	protected static final boolean validateContent(TextElement bodyEl, String content, Reference reference) {
		if(content.contains("/reference/")) {
			if(reference == null) {
				bodyEl.setErrorKey("error.reference.link.text");
				return false;
			}

			String submissionUrl = RecruitingHelper.getLinkToReference(reference);
			Matcher matcher = urlPattern.matcher(content);
			while (matcher.find()) {
				String url = matcher.group(1);
				if(url.contains("/reference/") && !url.equals(submissionUrl)) {
					bodyEl.setErrorKey("error.reference.link.text.ref", new String[] { submissionUrl });
					return false;
				}
			}
		}
		return true;
	}
	
	private static final boolean validateReferenceUrl(TextElement bodyEl, String url, Reference reference) {
		if(StringHelper.containsNonWhitespace(url)) {
			if(!validateRefereeUrlVar(url)) {
				bodyEl.setErrorKey("error.refereeUrl.link");
				return false;
			} else if(!validateRefereeUrl(url, reference)) {
				if(reference != null) {
					String submissionUrl = RecruitingHelper.getLinkToReference(reference);
					bodyEl.setErrorKey("error.reference.link.ref", new String[] { submissionUrl });
				} else {
					bodyEl.setErrorKey("error.reference.link");
				}
				return false;
			}
		} else {
			bodyEl.setErrorKey("error.reference.link.empty");
			return false;
		}
		return true;
	}
	
	private static final boolean validateRefereeUrl(String url, Reference reference) {
		if(url.contains("/reference/")) {
			if(reference == null) {
				return false;
			}
			
			String submissionUrl = RecruitingHelper.getLinkToReference(reference);
			if(!url.equals(submissionUrl)) {
				return false;
			}
		}
		return true;
	}
	
	private static final boolean validateRefereeUrlVar(String url) {
		String lurl = url.toLowerCase();
		return !lurl.contains("$refereeurl")
				&& !lurl.contains("$experturl")
				&& !lurl.contains("$referenceurl");
	}
	
	private static class LinksCollector extends DefaultHandler {
		
		private final List<String> urls = new ArrayList<>();
		private final StringBuilder content = new StringBuilder();
		
		public List<String> getUrls() {
			return urls;
		}
		
		public String getContent() {
			return content.toString();
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if(start >= 0 && length > 0 && ch != null && ch.length > 0 && ch.length <= start + length) {
				content.append(ch, start, length);
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if("a".equalsIgnoreCase(localName)) {
				String url = attributes.getValue("href");
				if(url != null) {
					urls.add(url);
				}
			}
			content.append(" ");
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			content.append(" ");
		}
	}

	public static final int compareStatus(ReferenceStatus status1, ReferenceRequestStatus requestStatus1, ReferenceStatus status2, ReferenceRequestStatus requestStatus2) {
		int s1 = getStatusValue(status1, requestStatus1); 
		int s2 = getStatusValue(status2, requestStatus2); 
		return Integer.compare(s1, s2);
	}
	
	private static final int getStatusValue(ReferenceStatus status, ReferenceRequestStatus requestStatus) {
		int c;
		if(status == ReferenceStatus.deactivated) {
			c = 100;
		} else if(requestStatus == ReferenceRequestStatus.declined) {
			c = 80;
		} else {
			c = status.ordinal() * 10;
			if(status == ReferenceStatus.sentAwaiting && requestStatus == ReferenceRequestStatus.accepted) {
				c += 3;
			}
		}
		return c;
	}
}