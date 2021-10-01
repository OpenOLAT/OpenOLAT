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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 * Description:<br>
 * The MailTemplate holds a mail subject/body template and the according methods
 * to populate the velocity contexts with the user values
 * <P>
 * Usage:<br>
 * Helper to create various mail templates used in the groupmanagement when
 * adding and removing users.
 * <p>
 * Initial Date: 23.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */

package org.olat.group.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.ui.main.BusinessGroupListController;
import org.olat.repository.RepositoryEntryShort;
import org.olat.user.UserManager;


public class BGMailHelper {

	/**
	 * The mail template when adding users to a group. The method chooses
	 * automatically the right translator for the given group type to customize
	 * the template text
	 * 
	 * @param group
	 * @param actor
	 * @return the generated MailTemplate
	 */
	public static MailTemplate createAddParticipantMailTemplate(BusinessGroupShort group, Identity actor) {
		String subjectKey = "notification.mail.added.subject";
		String bodyKey = "notification.mail.added.body";
		return createMailTemplate(group, actor, subjectKey, bodyKey, false);
	}

	/**
	 * The mail template when removing users from a group. The method chooses
	 * automatically the right translator for the given group type to customize
	 * the template text
	 * 
	 * @param group
	 * @param actor
	 * @return the generated MailTemplate
	 */
	public static MailTemplate createRemoveParticipantMailTemplate(BusinessGroupShort group, Identity actor) {
		String subjectKey = "notification.mail.removed.subject";
		String bodyKey = "notification.mail.removed.body";
		return createMailTemplate(group, actor, subjectKey, bodyKey, false);
	}

	/**
	 * The mail template when a user added himself to a group. The method chooses
	 * automatically the right translator for the given group type to customize
	 * the template text
	 * 
	 * @param group
	 * @param actor
	 * @return the generated MailTemplate
	 */
	public static MailTemplate createAddMyselfMailTemplate(BusinessGroupShort group, Identity actor) {
		String subjectKey = "notification.mail.added.self.subject";
		String bodyKey = "notification.mail.added.self.body";
		return createMailTemplate(group, actor, subjectKey, bodyKey, false);
	}

	/**
	 * The mail template when a user removed himself from a group. The method
	 * chooses automatically the right translator for the given group type to
	 * customize the template text
	 * 
	 * @param group
	 * @param actor
	 * @return the generated MailTemplate
	 */
	public static MailTemplate createRemoveMyselfMailTemplate(BusinessGroupShort group, Identity actor) {
		String subjectKey = "notification.mail.removed.self.subject";
		String bodyKey = "notification.mail.removed.self.body";
		return createMailTemplate(group, actor, subjectKey, bodyKey, false);
	}

	/**
	 * The mail template when adding users to a waitinglist. The method chooses
	 * automatically the right translator for the given group type to customize
	 * the template text
	 * 
	 * @param group
	 * @param actor
	 * @return the generated MailTemplate
	 */
	public static MailTemplate createAddWaitinglistMailTemplate(BusinessGroupShort group, Identity actor) {
		String subjectKey = "notification.mail.waitingList.added.subject";
		String bodyKey = "notification.mail.waitingList.added.body";
		return createMailTemplate(group, actor, subjectKey, bodyKey, false);
	}

	/**
	 * The mail template when removing users from a waiting list. The method
	 * chooses automatically the right translator for the given group type to
	 * customize the template text
	 * 
	 * @param group
	 * @param actor
	 * @return the generated MailTemplate
	 */
	public static MailTemplate createRemoveWaitinglistMailTemplate(BusinessGroupShort group, Identity actor) {
		String subjectKey = "notification.mail.waitingList.removed.subject";
		String bodyKey = "notification.mail.waitingList.removed.body";
		return createMailTemplate(group, actor, subjectKey, bodyKey, false);
	}

	/**
	 * The mail template when automatically transferring users from the
	 * waitinglist to the participants list adding users to a waitinglist. The
	 * method chooses automatically the right translator for the given group type
	 * to customize the template text
	 * 
	 * @param group
	 * @param actor
	 * @return the generated MailTemplate
	 */
	public static MailTemplate createWaitinglistTransferMailTemplate(BusinessGroupShort group, Identity actor) {
		String subjectKey = "notification.mail.waitingList.transfer.subject";
		String bodyKey = "notification.mail.waitingList.transfer.body";
		return createMailTemplate(group, actor, subjectKey, bodyKey, false);
	}

	/**
	 * Internal helper - does all the magic
	 * 
	 * @param group
	 * @param actor
	 * @param subjectKey
	 * @param bodyKey
	 * @return
	 */
	public static MailTemplate createMailTemplate(BusinessGroupShort group, Identity actor, String subjectKey, String bodyKey, boolean isCopy) {
		// get some data about the actor and fetch the translated subject / body via i18n module
		String[] bodyArgs = null;
		String lang = null;
		if (actor != null) {
			lang = actor.getUser().getPreferences().getLanguage();
		}
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(lang);
		if (actor != null) {
			bodyArgs = new String[] {
					actor.getUser().getProperty(UserConstants.FIRSTNAME, null),
					actor.getUser().getProperty(UserConstants.LASTNAME, null),
					UserManager.getInstance().getUserDisplayEmail(actor, locale),
					UserManager.getInstance().getUserDisplayEmail(actor, locale),// 2x for compatibility with old i18m properties
			};
		}
		
		return createMailTemplate(group, bodyArgs, subjectKey, bodyKey, locale, isCopy);
	}

	public static MailTemplate createMailTemplate(BusinessGroupShort group, String recepient, String subjectKey, String bodyKey, Locale locale, boolean isCopy) {
		String[] bodyArgs = new String[] {
				"",
				"",
				recepient,
				recepient
		};
		return createMailTemplate(group, bodyArgs, subjectKey, bodyKey, locale, isCopy);
	}
	
	public static String joinNames(Collection<BusinessGroup> groups) {
		StringBuilder names = new StringBuilder();
		for(BusinessGroup group:groups) {
			if(names.length() > 0) names.append(", ");
			names.append(group.getName());
		}
		return names.toString();
	}
	
	private static MailTemplate createMailTemplate(BusinessGroupShort group, String[] args, String subjectKey, String bodyKey, Locale locale, boolean isCopy) {
	
		Translator trans = Util.createPackageTranslator(BGMailHelper.class, locale,
				Util.createPackageTranslator(BusinessGroupListController.class, locale));
		String subject = trans.translate(subjectKey);
		String body = trans.translate(bodyKey, args);

		// build learning resources as list of url as string
		final BGMailTemplateInfos infos; 
		if(group != null) {
			BusinessGroupService businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
			List<RepositoryEntryShort> repoEntries = businessGroupService.findShortRepositoryEntries(Collections.singletonList(group), 0, -1);
			infos = getTemplateInfos(group, repoEntries);
			subject = subject.replace("$groupname", infos.getGroupName());
			body = body.replace("$groupname", infos.getGroupNameWithUrl());
			body = body.replace("$groupdescription", infos.getGroupDescription());
			if(StringHelper.containsNonWhitespace(infos.getCourseList())) {
				body = body.replace("$courselist", infos.getCourseList());
			} else {
				body = body.replace("$courselist", trans.translate("notification.mail.no.ressource", null));
			}
			
			if(isCopy) {
				String copy = trans.translate("notification.mail.copy.addition", new String[] { group.getName() });
				body = copy + body;
			}
		} else {
			infos = new BGMailTemplateInfos("", "", "", "");
		}
		
		return new BGMailTemplate(subject, body, infos, trans);
	}
	
	public static BGMailTemplateInfos getTemplateInfos(BusinessGroupShort group, List<RepositoryEntryShort> repoEntries) {
		StringBuilder learningResources = new StringBuilder();
		if(repoEntries != null && !repoEntries.isEmpty()) {
			for (RepositoryEntryShort entry: repoEntries) {
				String title = entry.getDisplayname();
				String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString("[RepositoryEntry:" + entry.getKey() + "]");
				learningResources.append(title);
				learningResources.append(" (");
				learningResources.append(url);
				learningResources.append(")\n");
			}
		}
		
		String courseList = null;
		if(learningResources.length() > 0) {
			courseList = learningResources.toString();
		}

		String groupNameWithUrl = null;
		String groupDescription = null;
		if(group != null) {
			// get group name and description
			StringBuilder sb = new StringBuilder();
			sb.append(group.getName() == null ? "" : group.getName())
			         .append(" (")
			         .append(BusinessControlFactory.getInstance().getURLFromBusinessPathString("[BusinessGroup:" + group.getKey() + "]"))
			         .append(")\n");
			groupNameWithUrl = sb.toString();
	
			String description;
			if(group instanceof BusinessGroup) {
				description = ((BusinessGroup)group).getDescription(); 
			} else {
				description = CoreSpringFactory.getImpl(BusinessGroupDAO.class).loadDescription(group.getKey());
			}
			groupDescription = FilterFactory.getHtmlTagAndDescapingFilter().filter(description);
		}
		return new BGMailTemplateInfos(group.getName(), groupNameWithUrl, groupDescription, courseList);
		
	}
	
	public static final class BGMailTemplateInfos {
		private final String groupName;
		private final String groupNameWithUrl;
		private final String groupDescription;
		private final String courseList;
		
		public BGMailTemplateInfos(String groupName, String groupNameWithUrl, String groupDescription, String courseList) {
			this.groupName = groupName;
			this.groupNameWithUrl = groupNameWithUrl;
			this.groupDescription = groupDescription;
			this.courseList = courseList;
		}
		
		public String getGroupName() {
			if(groupName == null) return "";
			return groupName;
		}
		
		public String getGroupNameWithUrl() {
			if(groupNameWithUrl == null) return "";
			return groupNameWithUrl;
		}
		
		public String getGroupDescription() {
			if(groupDescription == null) return "";
			return groupDescription;
		}
		
		public String getCourseList() {
			if(courseList == null) return "";
			return courseList;
		}
	}
	
	public static class BGMailTemplate extends MailTemplate {
		
		private static final String GROUP_NAME = "groupName";
		private static final String GROUP_DESCRIPTION = "groupDescription";
		private static final String COURSE_LIST = "courseList";
		private static final String COURSE_LIST_EMPTY = "courseListEmpty";
		
		private final BGMailTemplateInfos infos;
		private final Translator translator;
		
		public BGMailTemplate(String subject, String body, BGMailTemplateInfos infos, Translator translator) {
			super(subject, body, null);
			this.infos = infos;
			this.translator = translator;
		}
		
		public static final Collection<String> allVariableNames() {
			List<String> variableNames = new ArrayList<>();
			variableNames.addAll(getStandardIdentityVariableNames());
			variableNames.add(GROUP_NAME);
			variableNames.add(GROUP_DESCRIPTION);
			variableNames.add(COURSE_LIST);
			variableNames.add(COURSE_LIST_EMPTY);
			return variableNames;
		}
		
		@Override
		public Collection<String> getVariableNames() {
			List<String> variableNames = new ArrayList<>();
			variableNames.addAll(getStandardIdentityVariableNames());
			if (StringHelper.containsNonWhitespace(infos.getGroupNameWithUrl())) {
				variableNames.add(GROUP_NAME);
			}
			if (StringHelper.containsNonWhitespace(infos.getGroupDescription())) {
				variableNames.add(GROUP_DESCRIPTION);
			}
			if (StringHelper.containsNonWhitespace(infos.getCourseList())) {
				variableNames.add(COURSE_LIST);
				variableNames.add(COURSE_LIST_EMPTY);
			}
			return variableNames;
		}

		@Override
		public void putVariablesInMailContext(VelocityContext context, Identity identity) {
			fillContextWithStandardIdentityValues(context, identity, translator.getLocale());

			// Put user variables into velocity context
			if(identity != null) {
				//the email of the user, needs to stay named 'login'
				context.put("login", identity.getUser().getProperty(UserConstants.EMAIL, null));
			}
			// Put variables from greater context
			context.put(GROUP_NAME, infos.getGroupNameWithUrl());
			context.put("groupname", infos.getGroupNameWithUrl());
			context.put(GROUP_DESCRIPTION, infos.getGroupDescription());
			context.put("groupdescription", infos.getGroupDescription());
			if(StringHelper.containsNonWhitespace(infos.getCourseList())) {
				context.put(COURSE_LIST, infos.getCourseList());
				context.put("courselist", infos.getCourseList());
			} else {
				context.put(COURSE_LIST, translator.translate("notification.mail.no.ressource", null));
				context.put("courselist", translator.translate("notification.mail.no.ressource", null));
			}
			context.put(COURSE_LIST_EMPTY, translator.translate("notification.mail.no.ressource", null));
			context.put("courselistempty", translator.translate("notification.mail.no.ressource", null));
		}
	}
}
