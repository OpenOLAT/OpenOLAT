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

package org.olat.commons.info.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.olat.basesecurity.GroupRoles;
import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageManager;
import org.olat.commons.info.InfoMessageToCurriculumElement;
import org.olat.commons.info.InfoMessageToGroup;
import org.olat.commons.info.InfoSubscriptionManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.DateUtils;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 *
 * <p>
 * Initial Date:  27 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class SendMailStepController extends StepFormBasicController {

	private static final String ALL_COURSE_MEMBERS = "all.course.members";
	private static final String INDIVIDUAL_RECIPIENT = "individual";
	private static final String ONLY_NOTIFY_SUBS = "only.notify.subs";
	protected static final String SEND_TO_SUBS_AND_MAILS = "send.to.subs.mail";

	private final String[] sendSubscriberOptionKeys;
	private final String[] sendSubscriberOptionValues;
	private final String[] sendCourseRolesOptionKeys;
	private final String[] sendCourseRolesOptionValues;
	private String[] sendGroupsOptionKeys;
	private String[] sendGroupsOptionValues;
	private String[] sendCurriculaOptionKeys;
	private String[] sendCurriculaOptionValues;
	private MultipleSelectionElement sendSubscriberSelection;
	private MultipleSelectionElement sendCourseMemberSelection;
	private MultipleSelectionElement sendGroupMemberSelection;
	private MultipleSelectionElement sendCurriculumMemberSelection;
	private SingleSelection notificationEl;
	private SingleSelection recipientEl;
	private StaticTextElement publicationTextEl;

	@Autowired
	private InfoSubscriptionManager subscriptionManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private InfoMessageManager infoMessageManager;
	@Autowired
	private BusinessGroupService businessGroupService;


	public SendMailStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, SendMailOption subscriberOption,
								  List<SendMailOption> courseRoleOptions, List<SendMailOption> groupOptions, List<SendMailOption> curriculaOptions, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);

		// Subscriber option
		sendSubscriberOptionKeys = new String[]{subscriberOption.getOptionKey()};
		sendSubscriberOptionValues = new String[]{subscriberOption.getOptionName()};

		// Course members/roles option
		sendCourseRolesOptionKeys = new String[courseRoleOptions.size()];
		sendCourseRolesOptionValues = new String[courseRoleOptions.size()];
		int count = 0;
		for (SendMailOption option : courseRoleOptions) {
			sendCourseRolesOptionKeys[count] = option.getOptionKey();
			sendCourseRolesOptionValues[count++] = option.getOptionName();
		}

		// groups option
		if (groupOptions != null && !groupOptions.isEmpty()) {
			sendGroupsOptionKeys = new String[groupOptions.size()];
			sendGroupsOptionValues = new String[groupOptions.size()];

			int groupCount = 0;
			for (SendMailOption groupOption : groupOptions) {
				sendGroupsOptionKeys[groupCount] = groupOption.getOptionKey();
				sendGroupsOptionValues[groupCount++] = groupOption.getOptionName();
			}
		}

		// curricula options
		if (curriculaOptions != null && !curriculaOptions.isEmpty()) {
			sendCurriculaOptionKeys = new String[curriculaOptions.size()];
			sendCurriculaOptionValues = new String[curriculaOptions.size()];

			int curriculaCount = 0;
			for (SendMailOption curriculaOption : curriculaOptions) {
				sendCurriculaOptionKeys[curriculaCount] = curriculaOption.getOptionKey();
				sendCurriculaOptionValues[curriculaCount++] = curriculaOption.getOptionName();
			}
		}

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_info_contact");
		setFormTitle("wizard.step1.title");

		InfoMessage infoMessage = (InfoMessage) getFromRunContext(WizardConstants.MSG);
		// load object infoMessage, if it is in an editing process
		if (infoMessageManager.loadInfoMessageByKey(infoMessage.getKey()) != null) {
			infoMessage = infoMessageManager.loadInfoMessageByKey(infoMessage.getKey());
		}

		// publication info
		publicationTextEl = uifactory.addStaticTextElement("wizard.step1.publication", translate("wizard.step1.publication.immediately"), formLayout);
		// == because fixed memory addresses of constants and null safe
		if (getFromRunContext(WizardConstants.PUBLICATION_DATE_TYPE) == WizardConstants.PUBLICATION_DATE_SELECT_INDIVIDUAL) {
			// if publication is not immediately, calculate date difference and show in publication info
			DateChooser publishDate = (DateChooser) getFromRunContext(WizardConstants.PUBLICATION_DATE);
			String dayAsString = new SimpleDateFormat("EEEE", getLocale()).format(publishDate.getDate());
			String dateAsString = dayAsString + " " + publishDate.getValue();
			long daysBetween = ChronoUnit.DAYS.between(DateUtils.toLocalDate(new Date()), DateUtils.toLocalDate(publishDate.getDate()));

			publicationTextEl.setValue(translate("wizard.step1.publication.individual", String.valueOf(daysBetween), dateAsString));
		}

		// retrieve enabled subscribers of infoMessage courseElement
		OLATResourceable ores = (OLATResourceable) getFromRunContext("ores");
		String subPath = String.valueOf(getFromRunContext("subPath"));
		List<Identity> subscribers = subscriptionManager.getInfoSubscribers(ores, subPath);
		String noOfSubscribersString = String.valueOf(subscribers.size());

		// get courseMembers, to show the size
		List<Identity> members;
		if("BusinessGroup".equals(ores.getResourceableTypeName())) {
			BusinessGroup businessGroup = businessGroupService.loadBusinessGroup(ores.getResourceableId());
			members = businessGroupService.getMembers(businessGroup, GroupRoles.owner.name(), GroupRoles.coach.name(), GroupRoles.participant.name());
		} else {
			RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(ores, true);
			members = repositoryEntryRelationDao.getMembers(repositoryEntry, RepositoryEntryRelationType.all,
				GroupRoles.owner.name(), GroupRoles.coach.name(), GroupRoles.participant.name());
		}

		// notification cards, either only notify subscribers or notify subscriber and send e-mails
		SelectionValues notificationSV = new SelectionValues();
		notificationSV.add(entry(ONLY_NOTIFY_SUBS, translate("wizard.step1.subscription"), translate("wizard.step1.subscription.desc", noOfSubscribersString), "o_icon o_icon_rss", null, true));
		notificationSV.add(entry(SEND_TO_SUBS_AND_MAILS, translate("wizard.step1.subscription.email"), translate("wizard.step1.subscription.email.desc", noOfSubscribersString), "o_icon o_icon_mail", null, true));
		notificationEl = uifactory.addCardSingleSelectHorizontal("wizard.step1.notification", "wizard.step1.notification", formLayout, notificationSV);
		notificationEl.select(ONLY_NOTIFY_SUBS, true);
		notificationEl.addActionListener(FormEvent.ONCHANGE);
		notificationEl.setWidthInPercent(100, true);

		// recipient, either all course members or individual receivers
		SelectionValues recipientSV = new SelectionValues();
		recipientSV.add(entry(ALL_COURSE_MEMBERS, translate("wizard.step1.recipient.all", String.valueOf(members.size()))));
		recipientSV.add(entry(INDIVIDUAL_RECIPIENT, translate("wizard.step1.recipient.individual")));
		recipientEl = uifactory.addRadiosVertical("wizard.step1.recipient.selection", formLayout, recipientSV.keys(), recipientSV.values());
		recipientEl.select(ALL_COURSE_MEMBERS, true);
		recipientEl.addActionListener(FormEvent.ONCHANGE);

		// select if all subscribers should receive e-mails
		sendSubscriberSelection = uifactory.addCheckboxesVertical("indi.subscribers", formLayout, sendSubscriberOptionKeys, sendSubscriberOptionValues, 1);
		if (infoMessage.getSendMailTo() != null
				&& infoMessage.getSendMailTo().contains(WizardConstants.SEND_MAIL_SUBSCRIBERS)) {
			// pre-select checkbox, if this ui is shown while editing infoMessage, and it was selected before
			notificationEl.select(SEND_TO_SUBS_AND_MAILS, true);
			recipientEl.select(INDIVIDUAL_RECIPIENT, true);
			sendSubscriberSelection.select(WizardConstants.SEND_MAIL_SUBSCRIBERS, true);
		}

		// selection checkboxes for which course member roles should receive an e-mail
		sendCourseMemberSelection = uifactory.addCheckboxesVertical("indi.course.member", formLayout, sendCourseRolesOptionKeys, sendCourseRolesOptionValues, 1);
		if (infoMessage.getSendMailTo() != null) {
			// pre-select checkboxes, if this ui is shown while editing infoMessage, and it was selected before
			List<String> sendMailsTo = Arrays.stream(sendCourseRolesOptionKeys).filter(infoMessage.getSendMailTo()::contains).toList();
			if (!sendMailsTo.isEmpty()) {
				notificationEl.select(SEND_TO_SUBS_AND_MAILS, true);
				recipientEl.select(INDIVIDUAL_RECIPIENT, true);
				for (String sendMailTo : sendMailsTo) {
					sendCourseMemberSelection.select(sendMailTo, true);
				}
			}
		}

		// selection, which group members should receive e-mails
		sendGroupMemberSelection = uifactory.addCheckboxesVertical("indi.group.member", formLayout, sendGroupsOptionKeys != null ? sendGroupsOptionKeys : new String[]{}, sendGroupsOptionValues != null ? sendGroupsOptionValues : new String[]{}, 1);
		// for editing messages: set given data, if message is scheduled
		// check if publishDate is null, only messages which are getting edited have already a publishDate
		if (infoMessage.getPublishDate() != null) {
			// pre-select checkboxes, if this ui is shown while editing infoMessage, and it was selected before
			Set<InfoMessageToGroup> infoMessageToGroups = infoMessage.getGroups();
			if (!infoMessageToGroups.isEmpty()) {
				notificationEl.select(SEND_TO_SUBS_AND_MAILS, true);
				recipientEl.select(INDIVIDUAL_RECIPIENT, true);
				for (InfoMessageToGroup infoGroup : infoMessageToGroups) {
					sendGroupMemberSelection.select("send-mail-group-" + infoGroup.getBusinessGroup().getKey().toString(), true);
				}
			}
		}

		// selection, which curricula members should receive e-mails
		sendCurriculumMemberSelection = uifactory.addCheckboxesVertical("indi.curriculum.member", formLayout, sendCurriculaOptionKeys != null ? sendCurriculaOptionKeys : new String[]{}, sendCurriculaOptionValues != null ? sendCurriculaOptionValues : new String[]{}, 1);
		// for editing messages: set given data, if message is scheduled
		// check if publishDate is null, only messages which are getting edited have already a publishDate
		if (infoMessage.getPublishDate() != null) {
			Set<InfoMessageToCurriculumElement> infoMessageToCurriculumElements = infoMessage.getCurriculumElements();
			if (!infoMessageToCurriculumElements.isEmpty()) {
				notificationEl.select(SEND_TO_SUBS_AND_MAILS, true);
				recipientEl.select(INDIVIDUAL_RECIPIENT, true);
				for (InfoMessageToCurriculumElement infoCurEl : infoMessageToCurriculumElements) {
					sendCurriculumMemberSelection.select("send-mail-curriculum-" + infoCurEl.getCurriculumElement().getKey().toString(), true);
				}
			}
		}

		updateIndividualContainerVisibility();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		publicationTextEl.clearError();
		boolean allOk = super.validateFormLogic(ureq);

		Date publishDate = ((DateChooser) getFromRunContext(WizardConstants.PUBLICATION_DATE)).getDate();
		if (getFromRunContext(WizardConstants.PUBLICATION_DATE_TYPE) == WizardConstants.PUBLICATION_DATE_SELECT_INDIVIDUAL
				&& publishDate.before(new Date())) {
			publicationTextEl.setErrorKey("form.date.invalid.past");
			allOk = false;
		}

		return allOk;
	}

	private void updateIndividualContainerVisibility() {
		// Update visibility of UI elements
		recipientEl.setVisible(notificationEl.isKeySelected(SEND_TO_SUBS_AND_MAILS));
		sendSubscriberSelection.setVisible(recipientEl.isVisible() && recipientEl.isKeySelected(INDIVIDUAL_RECIPIENT));
		sendCourseMemberSelection.setVisible(recipientEl.isVisible() && recipientEl.isKeySelected(INDIVIDUAL_RECIPIENT));
		sendGroupMemberSelection.setVisible(recipientEl.isVisible()
				&& recipientEl.isKeySelected(INDIVIDUAL_RECIPIENT)
				&& !ArrayUtils.isEmpty(sendGroupsOptionKeys));
		sendCurriculumMemberSelection.setVisible(recipientEl.isVisible()
				&& recipientEl.isKeySelected(INDIVIDUAL_RECIPIENT)
				&& !ArrayUtils.isEmpty(sendCurriculaOptionKeys));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == recipientEl || source == notificationEl) {
			updateIndividualContainerVisibility();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// only if the option to notify subscribers and send mails is selected, it is necessary to add the selected options for sending mails
		// otherwise, nothing to do here
		if (notificationEl.isKeySelected(SEND_TO_SUBS_AND_MAILS)) {
			addToRunContext(WizardConstants.PUBLICATION_NOTIFICATION_TYPE, SEND_TO_SUBS_AND_MAILS);
			if (sendSubscriberSelection != null) {
				addToRunContext(WizardConstants.SEND_MAIL_SUBSCRIBERS, sendSubscriberSelection.isKeySelected(WizardConstants.SEND_MAIL_SUBSCRIBERS));
			}

			if (sendCourseMemberSelection != null) {
				if (recipientEl.isKeySelected(ALL_COURSE_MEMBERS)) {
					addToRunContext(WizardConstants.SEND_MAIL, sendCourseMemberSelection.getKeys());
				} else {
					addToRunContext(WizardConstants.SEND_MAIL, sendCourseMemberSelection.getSelectedKeys());
				}
			}

			if (sendGroupMemberSelection != null) {
				addToRunContext(WizardConstants.SEND_GROUPS, sendGroupMemberSelection.getSelectedKeys());
			}

			if (sendCurriculumMemberSelection != null) {
				addToRunContext(WizardConstants.SEND_CURRICULA, sendCurriculumMemberSelection.getSelectedKeys());
			}
		} else {
			addToRunContext(WizardConstants.PUBLICATION_NOTIFICATION_TYPE, ONLY_NOTIFY_SUBS);
		}

		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}