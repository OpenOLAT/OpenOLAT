/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.topicbroker.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.emptystate.EmptyStateItem;
import org.olat.core.gui.components.emptystate.EmptyStatePrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBAuditLogSearchParams;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBEnrollmentProcess;
import org.olat.modules.topicbroker.TBEnrollmentStats;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBParticipantCandidates;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.manager.DefaultEnrollmentProcess;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentManualProcessController extends FormBasicController {

	private static final String CMD_SELECT_RUN = "selrun_";
	private static final String[] KEYS = new String[] { "xx" };
	
	private FormSubmit applyLink;
	private FormLink runStartLink;
	private DropdownItem runsDropdown;
	private EmptyStateItem emptyState;
	private MultipleSelectionElement emailNotificationEl;

	private CloseableModalController cmc;
	private ConfirmationController doneConfirmationCtrl;
	private TBEnrollmentRunOverviewController enrollmentRunOverviewCtrl;

	private TBBroker broker;
	private final List<Identity> identities;
	private final List<TBParticipant> participants;
	private final List<TBTopic> topics;
	private final List<TBSelection> selections;
	private final List<EnrollmentProcessWrapper> runs = new ArrayList<>(3);
	private EnrollmentProcessWrapper selectedProcessWrapper;
	
	@Autowired
	private TopicBrokerService topicBrokerService;

	public TBEnrollmentManualProcessController(UserRequest ureq, WindowControl wControl, TBBroker broker,
			TBParticipantCandidates participantCandidates) {
		super(ureq, wControl, "enrollment_manual_process");
		this.broker = broker;
		participantCandidates.refresh();
		identities = participantCandidates.getAllIdentities();
		
		TBTopicSearchParams topicSearchParams = new TBTopicSearchParams();
		topicSearchParams.setBroker(broker);
		topics = topicBrokerService.getTopics(topicSearchParams);
		
		TBSelectionSearchParams selectionSearchParams = new TBSelectionSearchParams();
		selectionSearchParams.setBroker(broker);
		selectionSearchParams.setEnrolledOrIdentities(identities);
		selectionSearchParams.setEnrolledOrMaxSortOrder(broker.getMaxSelections());
		selectionSearchParams.setFetchParticipant(true);
		selections = topicBrokerService.getSelections(selectionSearchParams);
		
		participants = selections.stream().map(TBSelection::getParticipant).distinct().toList();
		
		initForm(ureq);
		updateEnrollmentDone();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		runStartLink = uifactory.addFormLink("enrollment.manual.run.start", formLayout, Link.BUTTON);
		runStartLink.setIconLeftCSS("o_icon o_icon-lg o_icon_tb_run_start");
		
		runsDropdown = uifactory.addDropdownMenu("enrollment.manual.runs", null, formLayout, getTranslator());
		runsDropdown.setOrientation(DropdownOrientation.right);
		runsDropdown.setIconCSS("o_icon o_icon-lg o_icon_tb_run");
		runsDropdown.setVisible(false);
		
		emptyState = uifactory.addEmptyState("emptyState", null, formLayout);
		emptyState.setIconCss("o_icon o_icon_tb_run_start");
		emptyState.setMessageI18nKey("enrollment.manual.empty");
		emptyState.setButtonI18nKey("enrollment.manual.run.start");
		emptyState.setButtonLeftIconCss("o_icon o_icon-lg o_icon_tb_run_start");
		
		enrollmentRunOverviewCtrl = new TBEnrollmentRunOverviewController(ureq, getWindowControl(), mainForm, broker);
		listenTo(enrollmentRunOverviewCtrl);
		formLayout.add("runOverview", enrollmentRunOverviewCtrl.getInitialFormItem());
		
		FormLayoutContainer emailCont = FormLayoutContainer.createDefaultFormLayout("emailCont", getTranslator());
		emailCont.setFormTitle(translate("enrollment.manual.email.title"));
		emailCont.setRootForm(mainForm);
		formLayout.add("email", emailCont);
		
		emailNotificationEl = uifactory.addCheckboxesHorizontal("enrollment.manual.email.notification", emailCont, KEYS,
				new String[] { translate("enrollment.manual.email.notification.value") });
		emailNotificationEl.select(KEYS[0], true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		applyLink = uifactory.addFormSubmitButton("enrollment.manual.apply", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private void updateEnrollmentDone() {
		broker = topicBrokerService.getBroker(broker);
		boolean enrollmentStarted = broker.getEnrollmentStartDate() != null;
		flc.contextPut("enrollmentDone", enrollmentStarted);
		
		boolean applyAvailable = !enrollmentStarted && !runs.isEmpty();
		applyLink.setEnabled(applyAvailable);
		applyLink.setSubmitAndValidate(applyAvailable);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (doneConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				saveEnrollments();
				fireEvent(ureq, FormEvent.DONE_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
		
	private void cleanUp() {
		removeAsListenerAndDispose(doneConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		doneConfirmationCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == runStartLink) {
			doRunEnrollmentProcess();
		} else if (source == emptyState) {
			if (event instanceof EmptyStatePrimaryActionEvent) {
				doRunEnrollmentProcess();
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if (cmd.startsWith(CMD_SELECT_RUN)) {
				int runNo = Integer.valueOf(cmd.substring(CMD_SELECT_RUN.length()));
				doSelectRun(runNo);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		updateEnrollmentDone();
		allOk &= applyLink.isVisible();
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		doConfirmApply(ureq);
	}
	
	private void doConfirmApply(UserRequest ureq) {
		String textI18nKey = emailNotificationEl.isAtLeastSelected(1)
				? "enrollment.manual.confirm.text.email" 
				: "enrollment.manual.confirm.text" ;
		doneConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate(textI18nKey, String.valueOf(selectedProcessWrapper.getStats().getNumEnrollments())),
				translate("enrollment.manual.confirm.confirm"),
				translate("enrollment.manual.confirm.button"));
		listenTo(doneConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), doneConfirmationCtrl.getInitialComponent(),
				true, translate("enrollment.manual.confirm.title"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void saveEnrollments() {
		if (selectedProcessWrapper != null) {
			if (isChangesSinceRun()) {
				showWarning("error.changes.since.last.run");
			} else {
				topicBrokerService.updateEnrollmentProcessStart(getIdentity(), broker);
				selectedProcessWrapper.getProcess().persist(getIdentity());
				topicBrokerService.updateEnrollmentProcessDone(getIdentity(), broker, emailNotificationEl.isAtLeastSelected(1));
			}
		}
	}
	
	private boolean isChangesSinceRun() {
		TBAuditLogSearchParams searchParams = new TBAuditLogSearchParams();
		searchParams.setBroker(broker);
		searchParams.setOrderAsc(Boolean.FALSE);
		Date lastAuditLogDate = topicBrokerService.getAuditLog(searchParams, 0, 1).get(0).getCreationDate();
		return lastAuditLogDate.after(selectedProcessWrapper.getStartDate());
	}

	private void doRunEnrollmentProcess() {
		Date startDate = new Date();
		TBEnrollmentProcess process = new DefaultEnrollmentProcess(broker, topics, selections);
		TBEnrollmentStats enrollmentStats = topicBrokerService.getEnrollmentStats(broker, identities, participants, process.getPreviewSelections());
		
		EnrollmentProcessWrapper wrapper = new EnrollmentProcessWrapper(startDate, process, enrollmentStats);
		runs.add(wrapper);
		
		int run = runs.size();
		String cmd = CMD_SELECT_RUN + run;
		FormLink selectPosLink = uifactory.addFormLink("selectp_" + run, cmd, "", null, flc, Link.NONTRANSLATED);
		selectPosLink.setI18nKey(translate("enrollment.manual.run.num", String.valueOf(run), String.valueOf(enrollmentStats.getNumEnrollments())));
		runsDropdown.addElement(selectPosLink);
		
		doSelectRun(run);
	}
	
	private void doSelectRun(int runNo) {
		EnrollmentProcessWrapper wrapper = runs.get(runNo - 1);
		selectedProcessWrapper = wrapper;
		
		flc.contextPut("displayRun", String.valueOf(runNo));
		runsDropdown.setTranslatedLabel(translate("enrollment.manual.run.num", String.valueOf(runNo),
				String.valueOf(selectedProcessWrapper.getStats().getNumEnrollments())));
		runsDropdown.setVisible(true);
		
		enrollmentRunOverviewCtrl.updateModel(wrapper.getStats());
		
		updateEnrollmentDone();
	}
	
	private static final class EnrollmentProcessWrapper {
		
		private final Date startDate;
		private final TBEnrollmentProcess process;
		private final TBEnrollmentStats stats;
		
		public EnrollmentProcessWrapper(Date startDate, TBEnrollmentProcess process, TBEnrollmentStats stats) {
			this.startDate = startDate;
			this.process = process;
			this.stats = stats;
		}

		public Date getStartDate() {
			return startDate;
		}

		public TBEnrollmentProcess getProcess() {
			return process;
		}

		public TBEnrollmentStats getStats() {
			return stats;
		}
		
	}
	
}
