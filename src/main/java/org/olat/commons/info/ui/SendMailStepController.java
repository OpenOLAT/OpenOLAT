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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
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
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.curriculum.CurriculumRoles;
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

	private final String[] sendSubscriberOptionKeys;
	private final String[] sendSubscriberOptionValues;
	private final String[] sendCourseRolesOptionKeys;
	private final String[] combinedCourseRoleOptionKeys;
	private String[] sendGroupsOptionKeys;
	private String[] sendCurriculaOptionKeys;
	private final boolean hasGroupsOrCurricula;

	private final List<SendMailOption> courseRoleOptions;
	private final List<SendMailOption> groupOptions;
	private final List<SendMailOption> curriculaOptions;

	private MultipleSelectionElement sendSubscriberSelection;
	// Case A: course (or business group) has no groups/curriculum elements attached
	private MultipleSelectionElement sendMembersSelection;
	// Case B: course has groups and/or curriculum elements attached
	private MultipleSelectionElement sendOwnerSelection;
	private MultipleSelectionElement sendCoachSelection;
	private MultipleSelectionElement sendParticipantSelection;
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

		this.courseRoleOptions = courseRoleOptions;
		this.groupOptions = groupOptions;
		this.curriculaOptions = curriculaOptions;

		// Subscriber option
		sendSubscriberOptionKeys = new String[]{subscriberOption.getOptionKey()};
		sendSubscriberOptionValues = new String[]{subscriberOption.getOptionName()};

		// Course members/roles option keys (values are built lazily in initForm, since building them
		// eagerly here would trigger their recipient-count queries twice)
		sendCourseRolesOptionKeys = courseRoleOptions.stream().map(SendMailOption::getOptionKey).toArray(String[]::new);
		combinedCourseRoleOptionKeys = Arrays.stream(sendCourseRolesOptionKeys)
				.filter(key -> !key.endsWith("-course"))
				.toArray(String[]::new);

		// groups option
		if (groupOptions != null && !groupOptions.isEmpty()) {
			sendGroupsOptionKeys = groupOptions.stream().map(SendMailOption::getOptionKey).toArray(String[]::new);
		}

		// curricula options
		if (curriculaOptions != null && !curriculaOptions.isEmpty()) {
			sendCurriculaOptionKeys = curriculaOptions.stream().map(SendMailOption::getOptionKey).toArray(String[]::new);
		}

		hasGroupsOrCurricula = !ArrayUtils.isEmpty(sendGroupsOptionKeys) || !ArrayUtils.isEmpty(sendCurriculaOptionKeys);

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
			DateFormat dateFormat = new SimpleDateFormat("HH:mm");
			// if publication is not immediately, calculate date difference and show in publication info
			DateChooser publishDate = (DateChooser) getFromRunContext(WizardConstants.PUBLICATION_DATE);
			String dayAsString = new SimpleDateFormat("EEEE", getLocale()).format(publishDate.getDate());
			String dateAsString = dayAsString + " "
					+ publishDate.getValue()
					+ " "
					+ translate("publication.at.time")
					+ " "
					+ dateFormat.format(publishDate.getDate());
			long daysBetween = ChronoUnit.DAYS.between(DateUtils.toLocalDate(new Date()), DateUtils.toLocalDate(publishDate.getDate()));

			// check if date is for today or later
			// change message accordingly
			String in;
			String inDays;
			if (daysBetween < 1) {
				in = "";
				inDays = translate("publication.today");
			} else if (daysBetween == 1) {
				in = "";
				inDays = translate("publication.tomorrow");
			} else {
				in = translate("publication.in.days");
				inDays = daysBetween + " " + translate("publication.later.days");
			}

			publicationTextEl.setValue(translate("wizard.step1.publication.individual", in, inDays, dateAsString));
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
		notificationSV.add(entry(WizardConstants.ONLY_NOTIFY_SUBS, translate("wizard.step1.subscription"), translate("wizard.step1.subscription.desc", noOfSubscribersString), "o_icon o_icon_rss", null, true));
		notificationSV.add(entry(WizardConstants.SEND_TO_SUBS_AND_MAILS, translate("wizard.step1.subscription.email"), translate("wizard.step1.subscription.email.desc", noOfSubscribersString), "o_icon o_icon_mail", null, true));
		notificationEl = uifactory.addCardSingleSelectHorizontal("wizard.step1.notification", "wizard.step1.notification", formLayout, notificationSV);
		notificationEl.addActionListener(FormEvent.ONCHANGE);
		notificationEl.setElementCssClass("o_radio_cards_lg");

		String notificationMode =
				infoMessage.isNotificationModeWithMail()
						? WizardConstants.SEND_TO_SUBS_AND_MAILS
						: WizardConstants.ONLY_NOTIFY_SUBS;
		notificationEl.select(notificationMode, true);

		// recipient, either all course members or individual receivers
		SelectionValues recipientSV = new SelectionValues();
		recipientSV.add(entry(WizardConstants.ALL_COURSE_MEMBERS, translate("wizard.step1.recipient.all", String.valueOf(members.size()))));
		recipientSV.add(entry(WizardConstants.INDIVIDUAL_RECIPIENT, translate("wizard.step1.recipient.individual")));
		recipientEl = uifactory.addRadiosVertical("wizard.step1.recipient.selection", formLayout, recipientSV.keys(), recipientSV.values());
		recipientEl.addActionListener(FormEvent.ONCHANGE);

		String recipientMode =
				infoMessage.isRecipientModeIndividual()
						? WizardConstants.INDIVIDUAL_RECIPIENT
						: WizardConstants.ALL_COURSE_MEMBERS;
		recipientEl.select(recipientMode, true);

		boolean isIndividualRecipient = recipientMode.equals(WizardConstants.INDIVIDUAL_RECIPIENT);
		Set<String> sendMailToKeys = parseSendMailToKeys(infoMessage);

		// select if all subscribers should receive e-mails
		sendSubscriberSelection = uifactory.addCheckboxesVertical("indi.subscribers", formLayout, sendSubscriberOptionKeys, sendSubscriberOptionValues, 1);
		if (isIndividualRecipient && sendMailToKeys.contains(WizardConstants.SEND_MAIL_SUBSCRIBERS)) {
			// pre-select checkbox, if this ui is shown while editing infoMessage, and it was selected before
			sendSubscriberSelection.select(WizardConstants.SEND_MAIL_SUBSCRIBERS, true);
		}

		// selection checkboxes for which member roles should receive an e-mail, grouped by role
		if (hasGroupsOrCurricula) {
			List<SendMailOption> ownerOptions = filterCourseRoleOptions(GroupRoles.owner);
			List<SendMailOption> coachOptions = new ArrayList<>(filterCourseRoleOptions(GroupRoles.coach));
			coachOptions.addAll(filterGroupOptions(GroupRoles.coach));
			coachOptions.addAll(filterCurriculumOptions(CurriculumRoles.coach));
			List<SendMailOption> participantOptions = new ArrayList<>(filterCourseRoleOptions(GroupRoles.participant));
			participantOptions.addAll(filterGroupOptions(GroupRoles.participant));
			participantOptions.addAll(filterCurriculumOptions(CurriculumRoles.participant));

			sendOwnerSelection = addOptionsCheckboxes("indi.owners", formLayout, ownerOptions, false);
			sendCoachSelection = addOptionsCheckboxes("indi.coaches", formLayout, coachOptions, true);
			sendParticipantSelection = addOptionsCheckboxes("indi.participants", formLayout, participantOptions, true);

			if (isIndividualRecipient) {
				// pre-select checkboxes, if this ui is shown while editing infoMessage, and it was selected before
				preSelectFromSendMailTo(sendOwnerSelection, sendMailToKeys);
				preSelectFromSendMailTo(sendCoachSelection, sendMailToKeys);
				preSelectFromSendMailTo(sendParticipantSelection, sendMailToKeys);

				// for editing messages: set given data, if message is scheduled
				// check if publishDate is null, only messages which are getting edited have already a publishDate
				if (infoMessage.getPublishDate() != null) {
					preSelectGroupsAndCurricula(infoMessage);
				}
			}
		} else {
			List<SendMailOption> memberOptions = new ArrayList<>();
			memberOptions.addAll(filterCourseRoleOptions(GroupRoles.owner));
			memberOptions.addAll(filterCourseRoleOptions(GroupRoles.coach));
			memberOptions.addAll(filterCourseRoleOptions(GroupRoles.participant));

			sendMembersSelection = addOptionsCheckboxes("indi.members", formLayout, memberOptions, false);
			if (isIndividualRecipient) {
				preSelectFromSendMailTo(sendMembersSelection, sendMailToKeys);
			}
		}

		updateIndividualContainerVisibility();
	}

	/**
	 * Course-role options (owner/coach/participant, plus the course-only coach/participant
	 * variants) for the given role, combined variant first, course-only variant second.
	 */
	private List<SendMailOption> filterCourseRoleOptions(GroupRoles role) {
		return courseRoleOptions.stream()
				.filter(option -> option.getOptionKey().equals(role.name()) || option.getOptionKey().equals(role.name() + "-course"))
				.sorted(Comparator.comparing(option -> option.getOptionKey().endsWith("-course")))
				.toList();
	}

	private List<SendMailOption> filterGroupOptions(GroupRoles role) {
		if (groupOptions == null) {
			return List.of();
		}
		return groupOptions.stream()
				.filter(SendMailGroupOption.class::isInstance)
				.map(SendMailGroupOption.class::cast)
				.filter(option -> option.getRole() == role)
				.sorted(Comparator.comparing(option -> option.getBusinessGroup().getName(), String.CASE_INSENSITIVE_ORDER))
				.map(SendMailOption.class::cast)
				.toList();
	}

	private List<SendMailOption> filterCurriculumOptions(CurriculumRoles role) {
		if (curriculaOptions == null) {
			return List.of();
		}
		return curriculaOptions.stream()
				.filter(SendMailCurriculumOption.class::isInstance)
				.map(SendMailCurriculumOption.class::cast)
				.filter(option -> option.getRole() == role)
				.sorted(Comparator.comparing(option -> option.getCurriculumElement().getDisplayName(), String.CASE_INSENSITIVE_ORDER))
				.map(SendMailOption.class::cast)
				.toList();
	}

	private MultipleSelectionElement addOptionsCheckboxes(String name, FormItemContainer formLayout, List<SendMailOption> options, boolean allowHtml) {
		String[] keys = options.stream().map(SendMailOption::getOptionKey).toArray(String[]::new);
		String[] values = options.stream().map(SendMailOption::getOptionName).toArray(String[]::new);
		MultipleSelectionElement element = uifactory.addCheckboxesVertical(name, formLayout, keys, values, 1);
		if (allowHtml) {
			// values may contain a muted HTML span for the CPL element reference (see SendMailCurriculumOption) -
			// every label sharing this widget must therefore already be escaped by its own SendMailOption
			element.setEscapeHtml(false);
		}
		return element;
	}

	private void preSelectFromSendMailTo(MultipleSelectionElement element, Set<String> sendMailToKeys) {
		for (String key : element.getKeys()) {
			if (sendMailToKeys.contains(key)) {
				element.select(key, true);
			}
		}
	}

	/**
	 * Pre-selects, for each linked group/curriculum element, exactly the role(s) recorded on its
	 * own InfoMessageToGroup/InfoMessageToCurriculumElement.sendMailTo value.
	 */
	private void preSelectGroupsAndCurricula(InfoMessage infoMessage) {
		Set<InfoMessageToGroup> infoMessageToGroups = infoMessage.getGroups();
		if (infoMessageToGroups != null) {
			for (InfoMessageToGroup infoGroup : infoMessageToGroups) {
				String groupKey = infoGroup.getBusinessGroup().getKey().toString();
				Set<String> roles = parseRoles(infoGroup.getSendMailTo());
				if (roles.contains(GroupRoles.coach.name())) {
					sendCoachSelection.select("send-mail-group-coach-" + groupKey, true);
				}
				if (roles.contains(GroupRoles.participant.name())) {
					sendParticipantSelection.select("send-mail-group-participant-" + groupKey, true);
				}
			}
		}

		Set<InfoMessageToCurriculumElement> infoMessageToCurriculumElements = infoMessage.getCurriculumElements();
		if (infoMessageToCurriculumElements != null) {
			for (InfoMessageToCurriculumElement infoCurEl : infoMessageToCurriculumElements) {
				String curriculumElementKey = infoCurEl.getCurriculumElement().getKey().toString();
				Set<String> roles = parseRoles(infoCurEl.getSendMailTo());
				if (roles.contains(CurriculumRoles.coach.name())) {
					sendCoachSelection.select("send-mail-curriculum-coach-" + curriculumElementKey, true);
				}
				if (roles.contains(CurriculumRoles.participant.name())) {
					sendParticipantSelection.select("send-mail-curriculum-participant-" + curriculumElementKey, true);
				}
			}
		}
	}

	private static Set<String> parseRoles(String sendMailTo) {
		return StringHelper.containsNonWhitespace(sendMailTo) ? new HashSet<>(Arrays.asList(sendMailTo.split(","))) : Set.of();
	}

	private Set<String> parseSendMailToKeys(InfoMessage infoMessage) {
		String sendMailTo = infoMessage.getSendMailTo();
		if (!StringHelper.containsNonWhitespace(sendMailTo)) {
			return Set.of();
		}
		return new HashSet<>(Arrays.asList(sendMailTo.split(",")));
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
		recipientEl.setVisible(notificationEl.isKeySelected(WizardConstants.SEND_TO_SUBS_AND_MAILS));
		boolean showIndividual = recipientEl.isVisible() && recipientEl.isKeySelected(WizardConstants.INDIVIDUAL_RECIPIENT);
		sendSubscriberSelection.setVisible(showIndividual);
		if (hasGroupsOrCurricula) {
			sendOwnerSelection.setVisible(showIndividual);
			sendCoachSelection.setVisible(showIndividual);
			sendParticipantSelection.setVisible(showIndividual);
		} else {
			sendMembersSelection.setVisible(showIndividual);
		}
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
		if (notificationEl.isKeySelected(WizardConstants.SEND_TO_SUBS_AND_MAILS)) {
			addToRunContext(WizardConstants.PUBLICATION_NOTIFICATION_TYPE, WizardConstants.SEND_TO_SUBS_AND_MAILS);
			if (sendSubscriberSelection != null) {
				addToRunContext(WizardConstants.SEND_MAIL_SUBSCRIBERS, sendSubscriberSelection.isKeySelected(WizardConstants.SEND_MAIL_SUBSCRIBERS));
			}

			addToRunContext(WizardConstants.RECIPIENT_MODE, recipientEl.getSelectedKey());
			if (recipientEl.isKeySelected(WizardConstants.ALL_COURSE_MEMBERS)) {
				// "All members": bypass the checkboxes entirely, same 3 historical keys as before this change;
				// group/curriculum-specific selections are never part of this bypass, matching prior behaviour
				addToRunContext(WizardConstants.SEND_MAIL, new LinkedHashSet<>(Arrays.asList(combinedCourseRoleOptionKeys)));
				addToRunContext(WizardConstants.SEND_GROUPS, Set.of());
				addToRunContext(WizardConstants.SEND_CURRICULA, Set.of());
			} else {
				Set<String> allSelected = new HashSet<>();
				if (hasGroupsOrCurricula) {
					allSelected.addAll(sendOwnerSelection.getSelectedKeys());
					allSelected.addAll(sendCoachSelection.getSelectedKeys());
					allSelected.addAll(sendParticipantSelection.getSelectedKeys());
				} else {
					allSelected.addAll(sendMembersSelection.getSelectedKeys());
				}

				addToRunContext(WizardConstants.SEND_MAIL, intersect(allSelected, sendCourseRolesOptionKeys));
				addToRunContext(WizardConstants.SEND_GROUPS, intersect(allSelected, sendGroupsOptionKeys));
				addToRunContext(WizardConstants.SEND_CURRICULA, intersect(allSelected, sendCurriculaOptionKeys));
			}
		} else {
			addToRunContext(WizardConstants.PUBLICATION_NOTIFICATION_TYPE, WizardConstants.ONLY_NOTIFY_SUBS);
		}

		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	private static Set<String> intersect(Set<String> selectedKeys, String[] candidateKeys) {
		if (ArrayUtils.isEmpty(candidateKeys)) {
			return Set.of();
		}
		Set<String> result = new LinkedHashSet<>();
		for (String key : candidateKeys) {
			if (selectedKeys.contains(key)) {
				result.add(key);
			}
		}
		return result;
	}
}
