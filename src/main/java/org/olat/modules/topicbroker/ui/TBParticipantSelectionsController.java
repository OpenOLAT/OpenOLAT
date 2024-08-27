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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.CodeHelper;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionStatus;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicRef;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.ui.TBSelectionDataModel.SelectionCols;
import org.olat.user.UserInfoProfile;
import org.olat.user.UserInfoProfileConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBParticipantSelectionsController extends FormBasicController {
	
	private static final String CMD_ENROLL = "enroll";
	private static final String CMD_WITHDRAW = "withdraw";
	private static final String CMD_UNSELECT = "unselect";
	
	private FormLink topicSelectLink;
	private final TBSelectionStatusRenderer statusRenderer;
	private TBSelectionDataModel selectionDataModel;
	private FlexiTableElement selectionTableEl;

	private TBParticipantController participantCtrl;
	private CloseableModalController cmc;
	private TBParticipantTopicSelectController topicSelectCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private SelectionToolsController selectionToolsCtrl;

	private TBBroker broker;
	private final TBParticipant participant;
	private final UserInfoProfileConfig profileConfig;
	private final UserInfoProfile profile;
	private final List<TBSelection> selections;
	private boolean canEditSelections;
	private boolean canEditParticipant;
	private final String toolsSuffix;
	
	@Autowired
	private TopicBrokerService topicBrokerService;

	public TBParticipantSelectionsController(UserRequest ureq, WindowControl wControl, Form mainForm, TBBroker broker,
			TBParticipant participant, UserInfoProfileConfig profileConfig, UserInfoProfile profile,
			List<TBSelection> selections, boolean canEditSelections) {
		super(ureq, wControl, LAYOUT_CUSTOM, "participant_selections", mainForm);
		
		// Show the same data as in the row. So no reload.
		this.broker = broker;
		this.participant = participant;
		this.profileConfig = profileConfig;
		this.profile = profile;
		this.selections = selections;
		this.canEditSelections = canEditSelections;
		this.canEditParticipant = canEditSelections && broker.getEnrollmentStartDate() == null;
		
		statusRenderer = new TBSelectionStatusRenderer();
		toolsSuffix = "_" + participant.getKey();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	public void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		participantCtrl = new TBParticipantController(ureq, getWindowControl(), mainForm, broker, participant,
				profileConfig, profile, canEditParticipant);
		listenTo(participantCtrl);
		String participantName = "participant_" + participant.getKey();
		formLayout.add(participantName, participantCtrl.getInitialFormItem());
		flc.contextPut("participantName", participantName);
		
		if (canEditSelections) {
			topicSelectLink = uifactory.addFormLink("participant.topic.select", formLayout, Link.BUTTON);
			topicSelectLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		}
		
		initSelectionTable(formLayout, ureq);
	}

	private void initSelectionTable(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel selectionColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		selectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.priority, new TextFlexiCellRenderer(EscapeMode.none)));
		
		selectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.title));
		selectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.status, statusRenderer));
		
		if (canEditSelections) {
			StickyActionColumnModel toolsCol = new StickyActionColumnModel(SelectionCols.selectionTools);
			toolsCol.setAlwaysVisible(true);
			toolsCol.setSortable(false);
			toolsCol.setExportable(false);
			selectionColumnsModel.addFlexiColumnModel(toolsCol);
		}
		
		selectionDataModel = new TBSelectionDataModel(selectionColumnsModel);
		String tableName = "selectionTable_" + participant.getKey();
		flc.contextPut("tableName", tableName);
		selectionTableEl = uifactory.addTableElement(getWindowControl(), tableName, selectionDataModel, 20, false, getTranslator(), formLayout);
		selectionTableEl.setAndLoadPersistedPreferences(ureq, "topic-broker-selection" + broker.getKey());
	}
	
	private void loadModel() {
		int requiredEnrollments = TBUIFactory.getRequiredEnrollments(broker, participant);
		int numEnrollments = (int)selections.stream().filter(TBSelection::isEnrolled).count();
		
		List<TBSelectionRow> selectionRows = new ArrayList<>();
		for (TBSelection selection : selections) {
			TBSelectionRow row = new TBSelectionRow();
			TBTopic topic = selection.getTopic();
			row.setTopic(topic);
			row.setTitleAbbr(TBUIFactory.getTitleAbbr(topic.getTitle()));
			row.setTopicSortOrder(topic.getSortOrder());
			
			row.setSelectionRef(selection);
			row.setSelectionSortOrder(selection.getSortOrder());
			row.setEnrolled(selection.isEnrolled());
			forgeStatus(row, requiredEnrollments, numEnrollments);
			row.setPriorityLabel(TBUIFactory.getPriorityLabelAsRow(getTranslator(), row.getStatus(), selection.getSortOrder()));
			
			forgeToolsLink(row);
			
			selectionRows.add(row);
		}
		
		selectionRows.sort((r1, r2) -> Integer.compare(r1.getSelectionSortOrder(), r2.getSelectionSortOrder()));
		fillEmptySelectionRows(selectionRows);
		selectionDataModel.setObjects(selectionRows);
		selectionTableEl.reset(false, false, true);
		
		updateSelectionMessage(requiredEnrollments, numEnrollments);
	}
	
	private void forgeToolsLink(TBSelectionRow row) {
		if (!canEditSelections) {
			return;
		}
		
		FormLink selectionToolsLink = uifactory.addFormLink("tools_" + CodeHelper.getRAMUniqueID(), "selectionTools" + toolsSuffix, "", null, null, Link.NONTRANSLATED);
		selectionToolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		selectionToolsLink.setUserObject(row);
		row.setSelectionToolsLink(selectionToolsLink);
	}

	private void fillEmptySelectionRows(List<TBSelectionRow> selectionRows) {
		for (int i = selectionRows.size() + 1; i <= broker.getMaxSelections(); i++) {
			TBSelectionRow row = new TBSelectionRow();
			row.setSelectionSortOrder(i);
			row.setPriorityLabel(TBUIFactory.getPriorityLabelAsRow(getTranslator(), TBSelectionStatus.fillIn, i));
			selectionRows.add(row);
		}
	}

	private void forgeStatus(TBSelectionRow row, int requiredEnrollments, int numEnrollments) {
		TBSelectionStatus status = TBUIFactory.getSelectionStatus(broker, requiredEnrollments, numEnrollments,
				true, row.isEnrolled(), row.getSelectionSortOrder());
		row.setStatus(status);
		row.setTranslatedStatus(TBUIFactory.getTranslatedStatus(getTranslator(), row.getStatus()));
		row.setStatusLabel(statusRenderer.render(getTranslator(), row));
	}

	private void updateSelectionMessage(int requiredEnrollments, int numEnrollments) {
		if (broker.getEnrollmentDoneDate() != null && numEnrollments < requiredEnrollments) {
			String selectionMsg = translate("participant.msg.too.less.enrollments.highlighted", 
					String.valueOf(numEnrollments), String.valueOf(requiredEnrollments));
			flc.contextPut("selectionMsg", selectionMsg);
		} else {
			flc.contextRemove("selectionMsg");
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (participantCtrl == source) {
			if (Event.CHANGED_EVENT == event) {
				fireEvent(ureq, event);
			}
		} else if (topicSelectCtrl == source) {
			if (Event.DONE_EVENT == event) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else if (selectionToolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if (toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(topicSelectCtrl);
		removeAsListenerAndDispose(selectionToolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(cmc);
		topicSelectCtrl = null; 
		selectionToolsCtrl = null;
		toolsCalloutCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == topicSelectLink){
			doSelectTopic(ureq);
		} else if (source instanceof FormLink link) {
			if (link.getUserObject() instanceof TBSelectionRow row) {
				String cmd = link.getCmd();
				if (cmd.startsWith("selectionTools") && cmd.endsWith(toolsSuffix)) {
					doOpenSelectionTools(ureq, row, link);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doSelectTopic(UserRequest ureq) {
		if (guardModalController(topicSelectCtrl)) return;
		
		topicSelectCtrl = new TBParticipantTopicSelectController(ureq, getWindowControl(), broker, participant.getIdentity(), selections);
		listenTo(topicSelectCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), topicSelectCtrl.getInitialComponent(),
				true, translate("participant.topic.select"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEnrollTopic(UserRequest ureq, TBTopicRef topic) {
		TBTopic reloadedTopic = topicBrokerService.getTopic(topic);
		
		if (canEditSelections && reloadedTopic.getDeletedDate() == null) {
			topicBrokerService.enroll(getIdentity(), participant.getIdentity(), reloadedTopic, false);
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doWithdrawTopic(UserRequest ureq, TBTopicRef topic) {
		TBTopic reloadedTopic = topicBrokerService.getTopic(topic);
		
		if (canEditSelections && reloadedTopic.getDeletedDate() == null) {
			topicBrokerService.withdraw(getIdentity(), participant.getIdentity(), reloadedTopic, false);
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doUnselectTopic(UserRequest ureq, TBTopicRef topic) {
		TBTopic reloadedTopic = topicBrokerService.getTopic(topic);
		
		if (canEditSelections && reloadedTopic.getDeletedDate() == null) {
			topicBrokerService.unselect(getIdentity(), participant.getIdentity(), topic);
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	
	private void doOpenSelectionTools(UserRequest ureq, TBSelectionRow row, FormLink link) {
		removeAsListenerAndDispose(selectionToolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		selectionToolsCtrl = new SelectionToolsController(ureq, getWindowControl(), row);
		listenTo(selectionToolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				selectionToolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class SelectionToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final TBSelectionRow row;
		private final List<String> names = new ArrayList<>(3);
		
		public SelectionToolsController(UserRequest ureq, WindowControl wControl, TBSelectionRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			putInitialPanel(mainVC);
			
			if (row.isEnrolled()) {
				addLink("withdraw", CMD_WITHDRAW, "o_icon o_icon-fw o_icon_tb_withdraw");
			} else {
				addLink("enroll", CMD_ENROLL, "o_icon o_icon-fw o_icon_tb_enroll");
			}
			
			names.add("divider");
			addLink("remove", CMD_UNSELECT, "o_icon o_icon-fw o_icon_tb_unselect");
			
			mainVC.contextPut("names", names);
		}
		
		private void addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if (iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			names.add(name);
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if (CMD_ENROLL.equals(cmd)) {
					doEnrollTopic(ureq, row.getTopic());
				} else if (CMD_WITHDRAW.endsWith(cmd)) {
					doWithdrawTopic(ureq, row.getTopic());
				} else if (CMD_UNSELECT.equals(cmd)) {
					doUnselectTopic(ureq, row.getTopic());
				}
			}
		}
	}

}
