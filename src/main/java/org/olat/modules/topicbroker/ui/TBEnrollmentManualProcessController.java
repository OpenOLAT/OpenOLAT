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

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
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
import org.olat.core.logging.Tracing;
import org.olat.modules.topicbroker.TBAuditLog;
import org.olat.modules.topicbroker.TBAuditLogSearchParams;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBEnrollmentProcess;
import org.olat.modules.topicbroker.TBEnrollmentProcessor;
import org.olat.modules.topicbroker.TBEnrollmentStats;
import org.olat.modules.topicbroker.TBEnrollmentStrategy;
import org.olat.modules.topicbroker.TBEnrollmentStrategyConfig;
import org.olat.modules.topicbroker.TBEnrollmentStrategyContext;
import org.olat.modules.topicbroker.TBEnrollmentStrategyFactory;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBParticipantCandidates;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.manager.TopicBrokerXStream;
import org.olat.modules.topicbroker.model.TBProcessInfos;
import org.olat.modules.topicbroker.ui.events.TBEnrollmentProcessRunEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentManualProcessController extends FormBasicController {
	
	private static final Logger log = Tracing.createLoggerFor(TBEnrollmentManualProcessController.class);

	private static final String CMD_SELECT_RUN = "selrun_";
	
	private FormLink runStartLink;
	private DropdownItem runsDropdown;
	private FormSubmit acceptLink;
	private FormLink acceptWithoutNotificationLink;

	private CloseableModalController cmc;
	private ConfirmationController doneConfirmationCtrl;
	private TBEnrollmentStrategyController enrollmentStrategyCtrl;
	private TBEnrollmentRunOverviewController enrollmentRunOverviewCtrl;

	private TBBroker broker;
	private final List<Identity> identities;
	private final List<TBParticipant> participants;
	private final List<TBTopic> topics;
	private final List<TBSelection> selections;
	private final List<EnrollmentProcessWrapper> runs = new ArrayList<>(3);
	private final TBEnrollmentStrategyContext strategyContext;
	private final List<TBEnrollmentStrategy> debugStrategies;
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
		
		strategyContext = TBEnrollmentStrategyFactory.createContext(broker, topics, selections);
		debugStrategies = initDebugStrategies();
		
		initForm(ureq);
		updateEnrollmentDone();
		doRunEnrollmentProcess();
	}

	private List<TBEnrollmentStrategy> initDebugStrategies() {
		if (log.isDebugEnabled()) {
			return List.of(
					TBEnrollmentStrategyFactory.createStrategy(TBEnrollmentStrategyFactory.createMaxEnrollmentsConfig(), strategyContext),
					TBEnrollmentStrategyFactory.createStrategy(TBEnrollmentStrategyFactory.createMaxPrioritiesConfig(), strategyContext),
					TBEnrollmentStrategyFactory.createStrategy(TBEnrollmentStrategyFactory.createMaxTopicsConfig(), strategyContext)
				);
		}
		return null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		runStartLink = uifactory.addFormLink("enrollment.manual.run.again", formLayout, Link.BUTTON);
		runStartLink.setIconLeftCSS("o_icon o_icon-lg o_icon_tb_run_start");
		runsDropdown = uifactory.addDropdownMenu("enrollment.manual.runs", null, formLayout, getTranslator());
		
		runsDropdown.setOrientation(DropdownOrientation.right);
		runsDropdown.setIconCSS("o_icon o_icon-lg o_icon_tb_run");
		runsDropdown.setVisible(false);
		
		enrollmentStrategyCtrl = new TBEnrollmentStrategyController(ureq, getWindowControl(), mainForm, broker);
		enrollmentStrategyCtrl.setStrategyConfig(TBEnrollmentStrategyFactory.getDefaultConfig());
		listenTo(enrollmentStrategyCtrl);
		formLayout.add("strategy", enrollmentStrategyCtrl.getInitialFormItem());
		
		enrollmentRunOverviewCtrl = new TBEnrollmentRunOverviewController(ureq, getWindowControl(), mainForm, broker, topics);
		listenTo(enrollmentRunOverviewCtrl);
		formLayout.add("runOverview", enrollmentRunOverviewCtrl.getInitialFormItem());
		
		FormLayoutContainer emailCont = FormLayoutContainer.createDefaultFormLayout("emailCont", getTranslator());
		emailCont.setFormTitle(translate("enrollment.manual.email.title"));
		emailCont.setRootForm(mainForm);
		formLayout.add("email", emailCont);
		
		acceptLink = uifactory.addFormSubmitButton("enrollment.manual.accept", formLayout);
		acceptWithoutNotificationLink = uifactory.addFormLink("enrollment.manual.accept.without.notification", formLayout, Link.BUTTON);
	}
	
	private void updateEnrollmentDone() {
		broker = topicBrokerService.getBroker(broker);
		boolean enrollmentStarted = broker.getEnrollmentStartDate() != null;
		flc.contextPut("enrollmentDone", enrollmentStarted);
		
		boolean acceptAvailable = !enrollmentStarted && !runs.isEmpty();
		acceptLink.setEnabled(acceptAvailable);
		acceptLink.setSubmitAndValidate(acceptAvailable);
		acceptWithoutNotificationLink.setEnabled(acceptAvailable);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == enrollmentStrategyCtrl) {
			if (event == TBEnrollmentProcessRunEvent.EVENT) {
				doRunEnrollmentProcess();
			}
		} else if (doneConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT && doneConfirmationCtrl.getUserObject() instanceof Boolean withNotification) {
				saveEnrollments(withNotification);
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
		} else if (source == acceptWithoutNotificationLink) {
			doConfirmAccept(ureq, Boolean.FALSE);
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
		allOk &= acceptLink.isVisible();
		
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		doConfirmAccept(ureq, Boolean.TRUE);
	}
	
	private void doConfirmAccept(UserRequest ureq, Boolean withNotification) {
		String textI18nKey = withNotification
				? "enrollment.manual.accept.text.email" 
				: "enrollment.manual.accept.text" ;
		doneConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate(textI18nKey, String.valueOf(selectedProcessWrapper.getStats().getNumEnrollments())),
				translate("enrollment.manual.accept.accept.confirm"),
				translate("enrollment.manual.accept.accept"));
		listenTo(doneConfirmationCtrl);
		doneConfirmationCtrl.setUserObject(withNotification);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), doneConfirmationCtrl.getInitialComponent(),
				true, translate("enrollment.manual.accept.title"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void saveEnrollments(boolean withNotification) {
		if (selectedProcessWrapper != null) {
			if (isChangesSinceRun()) {
				showWarning("error.changes.since.last.run");
			} else {
				topicBrokerService.updateEnrollmentProcessStart(getIdentity(), broker);
				selectedProcessWrapper.getProcess().persist(getIdentity());
				
				String before = TopicBrokerXStream.toXml(selectedProcessWrapper.getStrategyConfig());
				topicBrokerService.log(TBAuditLog.Action.brokerEnrollmentStrategy, before, null, null, broker, null, null, null);
				TBProcessInfos infos = TBProcessInfos.ofStats(selectedProcessWrapper.getStats(), selectedProcessWrapper.getBestStrategyValue());
				before = TopicBrokerXStream.toXml(infos);
				topicBrokerService.log(TBAuditLog.Action.brokerEnrollmentStrategyValue, before, null, null, broker, null, null, null);
				topicBrokerService.updateEnrollmentProcessDone(getIdentity(), broker, withNotification);
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
		TBEnrollmentStrategyConfig strategyConfig = enrollmentStrategyCtrl.getStrategyConfig();
		TBEnrollmentStrategy strategy = TBEnrollmentStrategyFactory.createStrategy(strategyConfig, strategyContext);
		TBEnrollmentProcessor processor = topicBrokerService.createProcessor(broker, topics, selections, strategy, debugStrategies);
		TBEnrollmentProcess process = processor.getBest();
		TBEnrollmentStats enrollmentStats = topicBrokerService.getEnrollmentStats(broker, identities, participants,
				topics, process.getPreviewSelections());
		
		Date startDate = new Date();
		EnrollmentProcessWrapper wrapper = new EnrollmentProcessWrapper(startDate, strategyConfig, process,
				 processor.getBestStrategyValue(), processor.getRuns(), enrollmentStats);
		runs.add(wrapper);
		
		int run = runs.size();
		String cmd = CMD_SELECT_RUN + run;
		FormLink selectPosLink = uifactory.addFormLink("selectp_" + run, cmd, "", null, flc, Link.NONTRANSLATED);
		selectPosLink.setI18nKey(getRunLinkText(run, enrollmentStats));
		runsDropdown.addElement(selectPosLink);
		
		doSelectRun(run);
	}
	
	private void doSelectRun(int runNo) {
		EnrollmentProcessWrapper wrapper = runs.get(runNo - 1);
		selectedProcessWrapper = wrapper;
		TBEnrollmentStats enrollmentStats = selectedProcessWrapper.getStats();
		
		runsDropdown.setTranslatedLabel(getRunLinkText(runNo, enrollmentStats));
		runsDropdown.setVisible(runs.size() > 1);
		
		flc.contextPut("processRunNo", String.valueOf(runNo));
		flc.contextPut("processRuns", String.valueOf(wrapper.getRuns()));
		
		enrollmentStrategyCtrl.setStrategyConfig(wrapper.getStrategyConfig());
		enrollmentRunOverviewCtrl.updateModel(wrapper.getStats());
		
		updateEnrollmentDone();
	}
	
	private String getRunLinkText(int run, TBEnrollmentStats enrollmentStats) {
		return translate("enrollment.manual.run", 
				String.valueOf(run),
				"<i class=\"o_icon o_icon_tb_enrollments\"> </i> ",
				String.valueOf(enrollmentStats.getNumEnrollments()),
				String.valueOf(enrollmentStats.getNumRequiredEnrollments()),
				"<i class=\"o_icon o_icon_tb_topics\"> </i> ",
				String.valueOf(enrollmentStats.getNumTopicsMinReached()),
				String.valueOf(enrollmentStats.getNumTopicsTotal())
			);
	}
	
	private static final class EnrollmentProcessWrapper {
		
		private final Date startDate;
		private final TBEnrollmentStrategyConfig strategyConfig;
		private final TBEnrollmentProcess process;
		private final double bestStrategyValue;
		private final long runs;
		private final TBEnrollmentStats stats;
		
		public EnrollmentProcessWrapper(Date startDate, TBEnrollmentStrategyConfig strategyConfig,
				TBEnrollmentProcess process, double bestStrategyValue, long runs, TBEnrollmentStats stats) {
			this.startDate = startDate;
			this.strategyConfig = strategyConfig;
			this.process = process;
			this.bestStrategyValue = bestStrategyValue;
			this.runs = runs;
			this.stats = stats;
		}

		public Date getStartDate() {
			return startDate;
		}

		public TBEnrollmentStrategyConfig getStrategyConfig() {
			return strategyConfig;
		}

		public TBEnrollmentProcess getProcess() {
			return process;
		}

		public double getBestStrategyValue() {
			return bestStrategyValue;
		}

		public long getRuns() {
			return runs;
		}

		public TBEnrollmentStats getStats() {
			return stats;
		}
		
	}
	
}
