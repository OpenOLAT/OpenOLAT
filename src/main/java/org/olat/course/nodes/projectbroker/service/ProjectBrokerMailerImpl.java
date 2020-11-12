/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.projectbroker.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.properties.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * @author guretzki
 */
@Service
public class ProjectBrokerMailerImpl implements ProjectBrokerMailer {
	
	private static final String ENROLLED_IDENTITY_FIRST_NAME = "enrolledIdentityFirstName";
	private static final String ENROLLED_IDENTITY_LAST_NAME = "enrolledIdentityLastName";
	private static final String ENROLLED_IDENTITY_USERNAME = "enrolledIdentityUsername";
	private static final String PROJECT_TITLE = "projectTitle";
	private static final String CURRENT_DATE = "currentDate";
	private static final String FIRSTNAME_PROJECT_MANAGER = "firstnameProjectManager";
	private static final String LASTNAME_PROJECT_MANAGER = "lastnameProjectManager";
	private static final String USERNAME_PROJECT_MANAGER = "usernameProjectManager";
	private static final Collection<String> VARIABLE_NAMES =
			List.of(ENROLLED_IDENTITY_FIRST_NAME, ENROLLED_IDENTITY_LAST_NAME, ENROLLED_IDENTITY_USERNAME,
					PROJECT_TITLE, CURRENT_DATE);
	private static final Collection<String> VARIABLE_NAMES_PROJECT_CHANGE =
			List.of(PROJECT_TITLE, CURRENT_DATE, FIRSTNAME_PROJECT_MANAGER, LASTNAME_PROJECT_MANAGER,
					USERNAME_PROJECT_MANAGER);
	
	private static final String KEY_ENROLLED_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.enrolled.to.participant.subject";
	private static final String KEY_ENROLLED_EMAIL_TO_PARTICIPANT_BODY    = "mail.enrolled.to.participant.body";
	
	private static final String KEY_ENROLLED_EMAIL_TO_MANAGER_SUBJECT     = "mail.enrolled.to.manager.subject";
	private static final String KEY_ENROLLED_EMAIL_TO_MANAGER_BODY        = "mail.enrolled.to.manager.body";

	private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.cancel.enrollment.to.participant.subject";
	private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_BODY    = "mail.cancel.enrollment.to.participant.body";

	private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_SUBJECT = "mail.cancel.enrollment.to.manager.subject";
	private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_BODY    = "mail.cancel.enrollment.to.manager.body";

	private static final String KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.project.changed.to.participant.subject";
	private static final String KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_BODY    = "mail.project.changed.to.participant.body";
	
	private static final String KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.project.deleted.to.participant.subject";
	private static final String KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY    = "mail.project.deleted.to.participant.body";
	
	private static final String KEY_REMOVE_CANDIDATE_EMAIL_SUBJECT = "mail.remove.candidate.subject";
	private static final String KEY_REMOVE_CANDIDATE_EMAIL_BODY    = "mail.remove.candidate.body";
	private static final String KEY_ACCEPT_CANDIDATE_EMAIL_SUBJECT = "mail.accept.candidate.subject";
	private static final String KEY_ACCEPT_CANDIDATE_EMAIL_BODY    = "mail.accept.candidate.body";
	private static final String KEY_ADD_CANDIDATE_EMAIL_SUBJECT = "mail.add.candidate.subject";
	private static final String KEY_ADD_CANDIDATE_EMAIL_BODY    = "mail.add.candidate.body";
	private static final String KEY_ADD_PARTICIPANT_EMAIL_SUBJECT = "mail.add.participant.subject";
	private static final String KEY_ADD_PARTICIPANT_EMAIL_BODY    = "mail.add.participant.body";
	private static final String KEY_REMOVE_PARTICIPANT_EMAIL_SUBJECT = "mail.remove.participant.subject";
	private static final String KEY_REMOVE_PARTICIPANT_EMAIL_BODY    = "mail.remove.participant.body";
	
	private static final Logger log = Tracing.createLoggerFor(ProjectBrokerMailerImpl.class);
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	
	// For Enrollment 
	@Override
	public MailerResult sendEnrolledEmailToParticipant(Identity enrolledIdentity, Project project, Translator pT) {
		return sendEmail(enrolledIdentity, project, 
				             pT.translate(KEY_ENROLLED_EMAIL_TO_PARTICIPANT_SUBJECT), 
				             pT.translate(KEY_ENROLLED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
	}

	@Override
	public MailerResult sendEnrolledEmailToManager(Identity enrolledIdentity, Project project, Translator pT) {
		List<Identity> coaches = businessGroupService
				.getMembers(project.getProjectGroup(), GroupRoles.coach.name());
		return sendEmailToGroup(coaches, enrolledIdentity, project, 
				                    pT.translate(KEY_ENROLLED_EMAIL_TO_MANAGER_SUBJECT), 
				                    pT.translate(KEY_ENROLLED_EMAIL_TO_MANAGER_BODY), pT.getLocale());
	}

	// For cancel enrollment
	@Override
	public MailerResult sendCancelEnrollmentEmailToParticipant(Identity enrolledIdentity, Project project, Translator pT) {
		return sendEmail(enrolledIdentity, project, 
        pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_SUBJECT), 
        pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
	}

	@Override
	public MailerResult sendCancelEnrollmentEmailToManager(Identity enrolledIdentity, Project project, Translator pT) {
		List<Identity> coaches = businessGroupService
				.getMembers(project.getProjectGroup(), GroupRoles.coach.name());
		return sendEmailToGroup(coaches, enrolledIdentity, project, 
        pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_SUBJECT), 
        pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_BODY), pT.getLocale());
	}

	// Project change
	@Override
	public MailerResult sendProjectChangedEmailToParticipants(Identity changer, Project project, Translator pT) {
		List<Identity> participants = businessGroupService
				.getMembers(project.getProjectGroup(), GroupRoles.participant.name());
		return sendEmailProjectChanged(participants, changer, project, 
				pT.translate(KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_SUBJECT), 
				pT.translate(KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
	}

	@Override
	public MailerResult sendProjectDeletedEmailToParticipants(Identity changer, Project project, Translator pT) {
		List<Identity> participants = businessGroupService
			.getMembers(project.getProjectGroup(), GroupRoles.participant.name());
		return sendEmailProjectChanged(participants, changer, project, 
				pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT), 
				pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
	}
	
	@Override
	public MailerResult sendProjectDeletedEmailToManager(Identity changer, Project project, Translator pT) {
		List<Identity> coaches = businessGroupService
				.getMembers(project.getProjectGroup(), GroupRoles.coach.name());
		return sendEmailProjectChanged(coaches, changer, project, 
        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT), 
        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
	}

	@Override
	public MailerResult sendProjectDeletedEmailToAccountManagers(Identity changer, Project project, CourseEnvironment courseEnv, CourseNode node, Translator pT){
		Long groupKey = null;
		Property accountManagerGroupProperty = courseEnv.getCoursePropertyManager().findCourseNodeProperty(node, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
		// Check if account-manager-group-key-property already exist
		if (accountManagerGroupProperty != null) {
			groupKey = accountManagerGroupProperty.getLongValue();
		} 
		if (groupKey != null) {
			BusinessGroup accountManagerGroup = businessGroupService.loadBusinessGroup(groupKey);
			List<Identity> participants = businessGroupService
					.getMembers(accountManagerGroup, GroupRoles.participant.name());
			return sendEmailProjectChanged(participants, changer, project, 
	        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT), 
	        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
    }
	  
		return null;
	}
	
	@Override
	public MailTemplate createRemoveAsCandiadateMailTemplate(Project project, Identity projectManager, Translator pT) {
		return createProjectChangeMailTemplate( project, projectManager, pT.translate(KEY_REMOVE_CANDIDATE_EMAIL_SUBJECT), pT.translate(KEY_REMOVE_CANDIDATE_EMAIL_BODY), pT.getLocale());
	}

	@Override
	public MailTemplate createAcceptCandiadateMailTemplate(Project project, Identity projectManager, Translator pT) {
		return createProjectChangeMailTemplate( project, projectManager, pT.translate(KEY_ACCEPT_CANDIDATE_EMAIL_SUBJECT), pT.translate(KEY_ACCEPT_CANDIDATE_EMAIL_BODY), pT.getLocale());
	}

	@Override
	public MailTemplate createAddCandidateMailTemplate(Project project, Identity projectManager, Translator pT) {
		return createProjectChangeMailTemplate( project, projectManager, pT.translate(KEY_ADD_CANDIDATE_EMAIL_SUBJECT), pT.translate(KEY_ADD_CANDIDATE_EMAIL_BODY), pT.getLocale());
	}

	@Override
	public MailTemplate createAddParticipantMailTemplate(Project project, Identity projectManager, Translator pT) {
		return createProjectChangeMailTemplate( project, projectManager, pT.translate(KEY_ADD_PARTICIPANT_EMAIL_SUBJECT), pT.translate(KEY_ADD_PARTICIPANT_EMAIL_BODY), pT.getLocale());
	}

	@Override
	public MailTemplate createRemoveParticipantMailTemplate(Project project, Identity projectManager, Translator pT) {
		return createProjectChangeMailTemplate( project, projectManager, pT.translate(KEY_REMOVE_PARTICIPANT_EMAIL_SUBJECT), pT.translate(KEY_REMOVE_PARTICIPANT_EMAIL_BODY), pT.getLocale());
	}

	//////////////////
  // Private Methods	
  //////////////////
	private MailerResult sendEmail(Identity enrolledIdentity, Project project, String subject, String body, Locale locale) {
		MailTemplate enrolledMailTemplate = createMailTemplate(project, enrolledIdentity, subject, body, locale );
		MailContext context = new MailContextImpl(project.getProjectBroker(), null, null);
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, enrolledIdentity, enrolledMailTemplate, null, null, result);
		if(bundle != null) {
			mailManager.sendMessage(bundle);
		}
		log.info(Tracing.M_AUDIT, "ProjectBroker: sendEmail to identity.name=" + enrolledIdentity.getKey() + " , mailerResult.returnCode=" + result.getReturnCode());
		return result;
	}

	private MailerResult sendEmailToGroup(List<Identity> group, Identity enrolledIdentity, Project project, String subject, String body, Locale locale) {
		MailTemplate enrolledMailTemplate = this.createMailTemplate(project, enrolledIdentity, subject, body, locale );
		// loop over all project manger
		StringBuilder identityKeys = new StringBuilder();
		for (Identity identity : group) {
			if (identityKeys.length()>0) identityKeys.append(",");
			identityKeys.append(identity.getKey());
		}
		MailContext context = new MailContextImpl(project.getProjectBroker(), null, null);
		String metaId = UUID.randomUUID().toString().replace("-", "");
		
		MailerResult result = new MailerResult();
		MailBundle[] bundles = mailManager.makeMailBundles(context, group, enrolledMailTemplate, null, metaId, result);
		result.append(mailManager.sendMessage(bundles));
		log.info(Tracing.M_AUDIT, "ProjectBroker: sendEmailToGroup: identities={} , mailerResult.returnCode={}", identityKeys, result.getReturnCode());
		return result;
	}

	private MailerResult sendEmailProjectChanged(List<Identity> group, Identity changer, Project project, String subject, String body, Locale locale) {
		MailTemplate enrolledMailTemplate = this.createProjectChangeMailTemplate(project, changer, subject, body, locale );
		// loop over all project manger
		StringBuilder identityKeys = new StringBuilder();
		for (Identity identity : group) {
			if (identityKeys.length()>0) identityKeys.append(",");
			identityKeys.append(identity.getKey());
		}
		MailContext context = new MailContextImpl(project.getProjectBroker(), null, null);
		MailerResult result = new MailerResult();
		MailBundle[] bundles = mailManager.makeMailBundles(context, group, enrolledMailTemplate, null, null, result);
		result.append(mailManager.sendMessage(bundles));
		log.info(Tracing.M_AUDIT, "ProjectBroker: sendEmailToGroup: identities={} , mailerResult.returnCode={}", identityKeys, result.getReturnCode());
		return result;
	}

	private MailTemplate createMailTemplate(Project project, Identity enrolledIdentity, String subject, String body, Locale locale) {	
		
		final String projectTitle = project.getTitle();
		final String currentDate  = Formatter.getInstance(locale).formatDateAndTime(new Date());
		final String firstNameEnrolledIdentity = enrolledIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null);
		final String lastnameEnrolledIdentity  = enrolledIdentity.getUser().getProperty(UserConstants.LASTNAME, null);
		final String usernameEnrolledIdentity  = enrolledIdentity.getName();
			
		return new MailTemplate(subject, body, null) {
			
			@Override
			public Collection<String> getVariableNames() {
				return VARIABLE_NAMES;
			}
			
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				context.put(ENROLLED_IDENTITY_FIRST_NAME, firstNameEnrolledIdentity);
				context.put("enrolled_identity_firstname", firstNameEnrolledIdentity);
				context.put(ENROLLED_IDENTITY_LAST_NAME, lastnameEnrolledIdentity);
				context.put("enrolled_identity_lastname", lastnameEnrolledIdentity);
				context.put(ENROLLED_IDENTITY_USERNAME, usernameEnrolledIdentity);
				context.put("enrolled_identity_username", usernameEnrolledIdentity);
				context.put(PROJECT_TITLE, projectTitle);
				context.put(CURRENT_DATE, currentDate);
			}
		};
	}

	private MailTemplate createProjectChangeMailTemplate(Project project, Identity changer, String subject, String body, Locale locale) {	
		
		final String projectTitle = project.getTitle();
		final String currentDate  = Formatter.getInstance(locale).formatDateAndTime(new Date());
		final String firstnameProjectManager = changer.getUser().getProperty(UserConstants.FIRSTNAME, null);
		final String lastnameProjectManager  = changer.getUser().getProperty(UserConstants.LASTNAME, null);
		final String usernameProjectManager  = changer.getName();
		
		return new MailTemplate(subject, body, null) {
			
			@Override
			public Collection<String> getVariableNames() {
				return VARIABLE_NAMES_PROJECT_CHANGE;
			}
			
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				context.put(PROJECT_TITLE, projectTitle);
				context.put(CURRENT_DATE, currentDate);
				context.put(FIRSTNAME_PROJECT_MANAGER, firstnameProjectManager);
				context.put(LASTNAME_PROJECT_MANAGER, lastnameProjectManager);
				context.put(USERNAME_PROJECT_MANAGER, usernameProjectManager);
			}
		};
	}

	

}
