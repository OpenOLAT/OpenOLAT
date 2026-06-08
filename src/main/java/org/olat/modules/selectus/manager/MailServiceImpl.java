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
package org.olat.modules.selectus.manager;

import static org.olat.modules.selectus.manager.ApplicationMailTemplate.DEFAULT_TEMPLATE;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.MailLogInfos;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMailTemplate;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.letter.LetterConfiguration;
import org.olat.modules.selectus.model.letter.LetterConfigurationXStream;
import org.olat.modules.selectus.model.letter.LetterLanguageConfiguration;
import org.olat.modules.selectus.model.mail.EmailVariables;
import org.olat.modules.selectus.model.mail.InvitationVariables;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.model.mail.PositionMailTemplateRef;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;
import org.olat.modules.selectus.ui.rejection.PositionMailCenterController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MailServiceImpl implements MailService {
	
	private static final Logger log = Tracing.createLoggerFor(MailServiceImpl.class);
	
	@Autowired
	private MailTemplateDAO mailTemplateDao;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	@Override
	public PositionMailTemplate createTemplate(Position position, String id, String name) {
		return mailTemplateDao.createTemplate(position, id, name);
	}

	@Override
	public List<PositionMailTemplate> getTemplates(PositionRef position) {
		return mailTemplateDao.getTemplates(position);
	}

	@Override
	public PositionMailTemplate getTemplate(PositionMailTemplate template) {
		return mailTemplateDao.getTemplate(template);
	}

	@Override
	public PositionMailTemplate updateTemplate(PositionMailTemplate template) {
		return mailTemplateDao.updateTemplate(template);
	}

	@Override
	public void deleteTemplate(PositionMailTemplate template) {
		mailTemplateDao.deleteTemplate(template);
	}

	@Override
	public EmailVariables getEmailVariables(Position position, Locale locale) {
		final EmailVariables emailVars = new EmailVariables(locale);
		emailVars.setPosition(position);
		PositionRole[] ratingRoles = recruitingModule.getRolesAllowedToRate();
		List<IdentityRef> committee = recruitingService.getCommitteeRefs(position, ratingRoles);
		emailVars.setCommittee(committee);
		List<UserRating> ratings = recruitingService.getRatings(position, committee);
		emailVars.setRatings(ratings);
		List<MailLogInfos> rejectionLog = recruitingService.getMailLog(position);
		emailVars.setMailLog(rejectionLog);
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		emailVars.setHeadOfCommittee(headOfCommittee);
		Identity secretary = recruitingService.getSecretary(position);
		emailVars.setSecretary(secretary);
		fillEmailVariables(emailVars, position, headOfCommittee, secretary, locale);
		return emailVars;
	}
	
	@Override
	public EmailVariables getEmailVariables(Position position, ApplicationRef application, Locale locale) {
		final EmailVariables emailVars = new EmailVariables(locale);
		emailVars.setPosition(position);
		PositionRole[] ratingRoles = recruitingModule.getRolesAllowedToRate();
		List<IdentityRef> committeeRefs = recruitingService.getCommitteeRefs(position, ratingRoles);
		emailVars.setCommittee(committeeRefs);
		ApplicationLight mailApp = recruitingService.getApplicationLight(position, application);
		emailVars.setSelectedApps(Collections.singletonList(mailApp));
		
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		emailVars.setHeadOfCommittee(headOfCommittee);
		Identity secretary = recruitingService.getSecretary(position);
		emailVars.setSecretary(secretary);

		fillEmailVariables(emailVars, position, headOfCommittee, secretary, locale);
		return emailVars;
	}
	
	private void fillEmailVariables(EmailVariables emailVars, Position position, Identity headOfCommittee, Identity secretary, Locale locale) {
		Locale[] templatesLocale = recruitingModule.getPositionLocales();
		List<PositionMailTemplate> positionTemplates = mailTemplateDao.getTemplates(position);
		List<PositionMailTemplateRef> mergedTemplates = getMailTemplates(positionTemplates, locale);

		Translator mailTranslator = getMailTranslator(locale);
		String defaultTemplateLabel = mailTranslator.translate("rejection.template.label.def");
		
		if(mergedTemplates.isEmpty()) {
			for(Locale templateLocale:templatesLocale) {
				emailVars.addTemplate(applicantMailTemplate(null, DEFAULT_TEMPLATE, defaultTemplateLabel,
						position, headOfCommittee, secretary, templateLocale, positionTemplates));
			}
		} else {
			// no template
			for(Locale templateLocale:templatesLocale) {
				emailVars.addTemplate(applicantMailTemplate(null, "-", "-", position, headOfCommittee, secretary,
						templateLocale, positionTemplates));
			}
			// predefined templates
			for(PositionMailTemplateRef templateRef:mergedTemplates) {
				for(Locale templateLocale:templatesLocale) {
					emailVars.addTemplate(applicantMailTemplate(templateRef.getKey(), templateRef.getId(), templateRef.getName(), position, headOfCommittee, secretary,
							templateLocale, positionTemplates));
				}
			}
		}
	}
	
	@Override
	public List<PositionMailTemplateRef> getMailTemplates(PositionRef position, Locale locale) {
		List<PositionMailTemplate> positionTemplates = mailTemplateDao.getTemplates(position);
		return getMailTemplates(positionTemplates, locale);
	}
		
	private final List<PositionMailTemplateRef> getMailTemplates(List<PositionMailTemplate> positionTemplates, Locale locale) {
		String[] mailTemplates = recruitingModule.getMailTemplateTitles();
		if(mailTemplates == null || mailTemplates.length == 0) {
			mailTemplates = new String[] { DEFAULT_TEMPLATE };
		}
		
		Translator mailTranslator = getMailTranslator(locale);
		List<PositionMailTemplateRef> refs = new ArrayList<>(mailTemplates.length + positionTemplates.size());

		for(String mailTemplate:mailTemplates) {
			String label = mailTranslator.translate("rejection.template.label." + mailTemplate.toLowerCase());
			if(StringHelper.containsNonWhitespace(label)) {
				refs.add(new PositionMailTemplateRef(null, mailTemplate, label));
			}
		}
		
		for(PositionMailTemplate positionTemplate:positionTemplates) {
			boolean system = false;
			for(PositionMailTemplateRef ref:refs) {
				if(ref.getId().equalsIgnoreCase(positionTemplate.getId())) {
					ref.setKey(positionTemplate.getKey());
					system = true;
				}
			}
			
			if(!system) {
				refs.add(new PositionMailTemplateRef(positionTemplate.getKey(),
						positionTemplate.getId(), positionTemplate.getName()));
			}
		}

		return refs;
	}
	
	@Override
	public SubjectAndBody rejectionTemplate(Position position, String templateName, Identity headOfCommittee, Locale templateLocale) {
		List<PositionMailTemplate> positionTemplates = mailTemplateDao.getTemplates(position);
		return rejectionTemplate(position, templateName, headOfCommittee, templateLocale, positionTemplates);
	}
	
	@Override
	public String toLetter(LetterConfiguration letterConfiguration, Locale locale) {
		if(letterConfiguration == null) return null;
		
		String letterTemplate = getLetterTemplate(locale);
		LetterLanguageConfiguration languageConfiguration = letterConfiguration.getConfiguration(locale);
		return new LetterConfigurationScanner()
				.render(letterTemplate, languageConfiguration);
	}

	@Override
	public MailAttachment toAttachment(String rawConfiguration, ApplicationShort application, Locale locale) {
		if(!StringHelper.containsNonWhitespace(rawConfiguration)) return null;
		
		LetterConfiguration letterConfiguration = LetterConfigurationXStream.fromXml(rawConfiguration);
		if(letterConfiguration == null) return null;
		
		String contentToPdf = toLetter(letterConfiguration, locale);
		String filename = letterFilename(letterConfiguration, application);
		return MailAttachment.toPdf(contentToPdf, filename);
	}
	
	private static String letterFilename(LetterConfiguration letterConfiguration, ApplicationShort app) {
		StringBuilder sb = new StringBuilder(64);
		if(app != null && app.getPerson() != null) {
			if(StringHelper.containsNonWhitespace(app.getPerson().getLastName())) {
				sb.append(StringHelper.transformDisplayNameToFileSystemName(app.getPerson().getLastName()));
			}
			
			if(StringHelper.containsNonWhitespace(app.getPerson().getFirstName())) {
				if(sb.length() > 0) {
					sb.append("_");
				}
				sb.append(StringHelper.transformDisplayNameToFileSystemName(app.getPerson().getFirstName()));
			}
		}
		
		String filename = letterConfiguration.getTitle();
		if(StringHelper.containsNonWhitespace(filename)) {
			if(filename.toLowerCase().endsWith(".pdf")) {
				filename = filename.substring(0, filename.length() - 4);
			}
			filename = StringHelper.transformDisplayNameToFileSystemName(filename);
			if(sb.length() > 0) {
				sb.append("_");
			}
			sb.append(filename).append(".pdf");
		} else {
			sb.append("Letter.pdf");
		}
		return sb.toString();
	} 

	@Override
	public String getLetterTemplate(Locale locale) {
		String templateName;
		if("de".equals(locale.getLanguage())) {
			templateName = "letters_de.html";
		} else if("fr".equals(locale.getLanguage())) {
			templateName = "letters_fr.html";
		} else {
			templateName = "letters.html";
		}
		try(InputStream inXslt = RecruitingMainController.class.getResourceAsStream(templateName)) {
			return IOUtils.toString(inXslt, StandardCharsets.UTF_8);
		} catch(Exception e) {
			log.error("", e);
			return "";
		}
	}
	
	private final ApplicationMailTemplate applicantMailTemplate(Long key, String template, String templateLabel, Position position, Identity headOfCommittee, Identity secretary,
			Locale templateLocale, List<PositionMailTemplate> positionTemplates) {
		SubjectAndBody subjectAndBody = rejectionTemplate(position, template, headOfCommittee, templateLocale, positionTemplates);
		Translator translator = getMailTranslator(templateLocale);
		return new RecruitingMailTemplate(key, template, templateLabel,
				subjectAndBody.getSubject(), subjectAndBody.getBody(), subjectAndBody.getLetter(),
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
	}
	
	private final Translator getMailTranslator(Locale locale) {
		return Util.createPackageTranslator(PositionMailCenterController.class, locale,
				Util.createPackageTranslator(PositionController.class, locale));
	}
	
	private final SubjectAndBody rejectionTemplate(Position position, String templateName, Identity headOfCommittee, Locale templateLocale,
			List<PositionMailTemplate> positionTemplates) {
		OrganisationUnit organisationSettings = recruitingService.getOrganisationUnit(position);
		String staffMail = recruitingModule.getStaffMail(position, organisationSettings);
		Translator translator = getMailTranslator(templateLocale);
		
		String headLastname = "";
		String headFirstname = "";
		String headTitleFullname = "";
		if(headOfCommittee != null ) {
			headLastname = headOfCommittee.getUser().getProperty(UserConstants.LASTNAME, templateLocale);
			headFirstname = headOfCommittee.getUser().getProperty(UserConstants.FIRSTNAME, templateLocale);
			headTitleFullname = RecruitingHelper.formatFullNameWithTitle(headOfCommittee, templateLocale);
		}
		
		String[] args = new String[]{
				position.getMLTitle(templateLocale), 	// 0
				staffMail,								// 1
				headLastname,							// 2
				headFirstname,							// 3
				headTitleFullname						// 4
		};
		
		if(positionTemplates != null) {
			for(PositionMailTemplate template:positionTemplates) {
				if(templateName.equalsIgnoreCase(template.getId()) || templateName.equals(template.getKey().toString())) {
					MailAttachment letter = toAttachment(template.getLetter(), null, templateLocale);
					return new SubjectAndBody(template.getSubject(templateLocale), template.getBody(templateLocale), letter);
				}
			}
		}
		
		String templatePrefix = DEFAULT_TEMPLATE.equals(templateName) ? "" : templateName.toLowerCase() + ".";
		String subject = translator.translate("rejection.mail." + templatePrefix + "subject", args);
		String body = translator.translate("rejection.mail." + templatePrefix + "body", args);
		return new SubjectAndBody(subject, body, null);
	}
	
	@Override
	public InvitationVariables getInvitationVariables(Position position, Locale templateLocale) {
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		Identity secretary = recruitingService.getSecretary(position);
		
		final InvitationVariables invitationVar = new InvitationVariables();
		invitationVar.setPosition(position);
		
		Translator translator = getMailTranslator(templateLocale);

		SubjectAndBody subjectAndBody = ReferenceHelper.referenceTemplateBase(headOfCommittee, position, null, null,
				ReferenceType.expert, salutationGenerator, translator);
		ApplicationMailTemplate expertTemplate = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
				subjectAndBody.getSubject(), subjectAndBody.getBody(), subjectAndBody.getLetter(),
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
		invitationVar.setExpertTemplate(expertTemplate);
		
		subjectAndBody = ReferenceHelper.referenceTemplateBase(headOfCommittee, position, null, null,
				ReferenceType.comparativeAssessmentExpert, salutationGenerator, translator);
		ApplicationMailTemplate comparativeExpertTemplate = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
				subjectAndBody.getSubject(), subjectAndBody.getBody(), subjectAndBody.getLetter(),
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
		invitationVar.setComparativeExpertTemplate(comparativeExpertTemplate);
		
		subjectAndBody = ReferenceHelper.referenceTemplateBase(headOfCommittee, position, null, null,
				ReferenceType.recommendation, salutationGenerator, translator);
		ApplicationMailTemplate recommendationTemplate = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
				subjectAndBody.getSubject(), subjectAndBody.getBody(), subjectAndBody.getLetter(),
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
		invitationVar.setRecommendationTemplate(recommendationTemplate);
		
		return invitationVar;
	}
}
