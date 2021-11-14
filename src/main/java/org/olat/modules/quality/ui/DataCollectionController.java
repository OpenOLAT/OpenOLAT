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
package org.olat.modules.quality.ui;

import static org.olat.modules.quality.QualityDataCollectionStatus.FINISHED;
import static org.olat.modules.quality.QualityDataCollectionStatus.PREPARATION;
import static org.olat.modules.quality.QualityDataCollectionStatus.READY;
import static org.olat.modules.quality.QualityDataCollectionStatus.RUNNING;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.event.DataCollectionEvent;
import org.olat.modules.quality.ui.event.DataCollectionEvent.Action;
import org.olat.modules.quality.ui.security.DataCollectionSecurityCallback;
import org.olat.modules.quality.ui.security.QualitySecurityCallbackFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionController extends BasicController implements TooledController, Activateable2 {

	private static final String ORES_REPORT_TYPE = "report";
	
	private Link statusPreparationLink;
	private Link statusReadyLink;
	private Link statusRunningLink;
	private Link statusFinishedLink;
	private Link deleteLink;
	private Link configurationLink;
	private Link participantsLink;
	private Link remindersLink;
	private Link reportAccessLink;
	private Link reportLink;
	private Link previousReportLink;
	private Link followUpReportLink;
	private Component statusCmp;
	private final ButtonGroupComponent segmentButtonsCmp;
	private final TooledStackedPanel stackPanel;
	private final StackedPanel mainPanel;
	
	private DataCollectionConfigurationController configurationCtrl;
	private ParticipationListController participationsCtrl;
	private RemindersController remindersCtrl;
	private DataCollectionReportAccessController reportAccessCtrl;
	private DataCollectionReportController reportCtrl;
	private DataCollectionReportController previousReportCtrl;
	private DataCollectionReportController followUpReportCtrl;
	private CloseableModalController cmc;
	private DataCollectionStartConfirmationController startConfirmationController;
	private DataCollectionFinishConfirmationController finishConfirmationController;
	
	private DataCollectionSecurityCallback secCallback;
	private QualityDataCollection dataCollection;
	private List<Organisation> organisations;
	private QualityDataCollection previousDataCollection;
	private QualityDataCollection followUpDataCollection;
	
	@Autowired
	private QualityService qualityService;

	protected DataCollectionController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			QualityDataCollectionLight dataCollectionLight) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.dataCollection = qualityService.loadDataCollectionByKey(dataCollectionLight);
		organisations = qualityService.loadDataCollectionOrganisations(dataCollectionLight);
		secCallback = QualitySecurityCallbackFactory.createDataCollectionSecurityCallback(
				ureq.getUserSession().getRoles(), dataCollectionLight, organisations);
		
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		if (secCallback.canViewDataCollectionConfigurations()) {
			configurationLink = LinkFactory.createLink("data.collection.configuration", getTranslator(), this);
			segmentButtonsCmp.addButton(configurationLink, false);
			participantsLink = LinkFactory.createLink("data.collection.participations", getTranslator(), this);
			segmentButtonsCmp.addButton(participantsLink, false);
			remindersLink = LinkFactory.createLink("data.collection.reminders", getTranslator(), this);
			segmentButtonsCmp.addButton(remindersLink, false);
			reportAccessLink = LinkFactory.createLink("data.collection.report.access", getTranslator(), this);
			segmentButtonsCmp.addButton(reportAccessLink, false);
		}
		addReportButtons();
		
		mainPanel = putInitialPanel(new SimpleStackedPanel("dataCollectionSegments"));
		mainPanel.setContent(new Panel("empty"));
	}
	
	private void addReportButtons() {
		if (secCallback.canViewReport()) {
			if (segmentButtonsCmp.getComponent("data.collection.report") == null) {
				reportLink = LinkFactory.createLink("data.collection.report", getTranslator(), this);
				segmentButtonsCmp.addButton(reportLink, false);
			}
			previousDataCollection = qualityService.loadPrevious(dataCollection);
			if (previousDataCollection != null && segmentButtonsCmp.getComponent("data.collection.report.previous") == null) {
				previousReportLink = LinkFactory.createLink("data.collection.report.previous", getTranslator(), this);
				segmentButtonsCmp.addButton(previousReportLink, false);
			}
			followUpDataCollection = qualityService.loadFollowUp(dataCollection);
			if (followUpDataCollection != null && segmentButtonsCmp.getComponent("data.collection.report.followup") == null) {
				followUpReportLink = LinkFactory.createLink("data.collection.report.followup", getTranslator(), this);
				segmentButtonsCmp.addButton(followUpReportLink, false);
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries != null && !entries.isEmpty()) {
			OLATResourceable resource = entries.get(0).getOLATResourceable();
			if (ORES_REPORT_TYPE.equalsIgnoreCase(resource.getResourceableTypeName())
					&& secCallback.canViewReport()) {
				doOpenReport(ureq);
			}
		} else if (secCallback.canViewDataCollectionConfigurations()) {
			doOpenConfiguration(ureq);
		} else if (secCallback.canViewReport()) {
			doOpenReport(ureq);
		}
	}

	@Override
	public void initTools() {
		stackPanel.addTool(segmentButtonsCmp, true);
		initStatusTools();
		initButtons();
	}
	
	private void initStatusTools() {
		stackPanel.removeTool(statusCmp, this);
		if (canChangeStatus()) {
			statusCmp = buildStatusDrowdown();
		} else {
			statusCmp = buildStatusLink();
		}
		stackPanel.addTool(statusCmp, Align.left, true, null, this);
	}

	private boolean canChangeStatus() {
		return secCallback.canSetPreparation()
				|| secCallback.canSetReady()
				|| secCallback.canSetRunning()
				|| secCallback.canSetFinished();
	}

	private Dropdown buildStatusDrowdown() {
		QualityDataCollectionStatus actualStatus = dataCollection.getStatus();
	
		Dropdown statusDropdown = new Dropdown("process.states", "data.collection.status", false, getTranslator());
		statusDropdown.setElementCssClass("o_qual_tools_status o_with_labeled");
		statusDropdown.setIconCSS("o_icon o_icon-fw o_icon_qual_dc_" + actualStatus.name().toLowerCase());
		statusDropdown.setInnerText(translate("data.collection.status." + actualStatus.name().toLowerCase()));
		statusDropdown.setInnerCSS("o_labeled o_qual_dc_status_" + actualStatus.name().toLowerCase());
		statusDropdown.setOrientation(DropdownOrientation.normal);
	
		statusPreparationLink = LinkFactory.createToolLink("data.collection.status.preparation", translate("data.collection.status.preparation"), this);
		statusPreparationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_preparation");
		statusPreparationLink.setElementCssClass("o_labeled o_qual_dc_status_preparation");
		statusPreparationLink.setVisible(secCallback.canSetPreparation());
		statusDropdown.addComponent(statusPreparationLink);
	
		statusReadyLink = LinkFactory.createToolLink("data.collection.status.ready", translate("data.collection.status.ready"), this);
		statusReadyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_ready");
		statusReadyLink.setElementCssClass("o_labeled o_qual_dc_status_ready");
		statusReadyLink.setVisible(secCallback.canSetReady());
		statusDropdown.addComponent(statusReadyLink);
		
		statusRunningLink = LinkFactory.createToolLink("data.collection.status.running", translate("data.collection.status.running"), this);
		statusRunningLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_running");
		statusRunningLink.setElementCssClass("o_labeled o_qual_dc_status_running");
		statusRunningLink.setVisible(secCallback.canSetRunning());
		statusDropdown.addComponent(statusRunningLink);
		
		statusFinishedLink = LinkFactory.createToolLink("data.collection.status.finished", translate("data.collection.status.finished"), this);
		statusFinishedLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_finished");
		statusFinishedLink.setElementCssClass("o_labeled o_qual_dc_status_finished");
		statusFinishedLink.setVisible(secCallback.canSetFinished());
		statusDropdown.addComponent(statusFinishedLink);
		
		return statusDropdown;
	}

	private Component buildStatusLink() {
		QualityDataCollectionStatus actualStatus = dataCollection.getStatus();
		Link statusLink = LinkFactory.createToolLink("status.link",
				translate("data.collection.status." + actualStatus.name().toLowerCase()), this);
		statusLink.setElementCssClass("o_labeled o_qual_dc_status_" + actualStatus.name().toLowerCase());
		statusLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_" + actualStatus.name().toLowerCase());
		return statusLink;
	}

	private void initButtons() {
		if (deleteLink != null) stackPanel.removeTool(deleteLink, this);
		if (secCallback.canDeleteDataCollection() ) {
			deleteLink = LinkFactory.createToolLink("data.collection.delete", translate("data.collection.delete"),
					this);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_delete");
			stackPanel.addTool(deleteLink, Align.left, true, null, this);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof DataCollectionEvent) {
			DataCollectionEvent dccEvent = (DataCollectionEvent) event;
			Action action = dccEvent.getAction();
			if (Action.CHANGED.equals(action)) {
				dataCollection = dccEvent.getDataCollection();
				stackPanel.changeDisplayname(dataCollection.getTitle(), null, this);
			} else {
				fireEvent(ureq, event);
			}
		} else if (source == startConfirmationController) {
			if (event.equals(Event.DONE_EVENT)) {
				doSetStatusRunning(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == finishConfirmationController) {
			if (event.equals(Event.DONE_EVENT)) {
				doSetStatusFinished(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == statusPreparationLink) {
			doSetStatusPreparation(ureq);
		} else if (source == statusReadyLink) {
			doSetStatusReady(ureq);
		} else if (source == statusRunningLink) {
			doConfirmStatusRunning(ureq);
		} else if (source == statusFinishedLink) {
			doConfirmStatusFinished(ureq);
		} else if (source == deleteLink) {
			fireEvent(ureq, new DataCollectionEvent(dataCollection, Action.DELETE));
		} else if (configurationLink == source) {
			doOpenConfiguration(ureq);
		} else if(participantsLink == source) {
			doOpenParticipants(ureq);
		} else if(remindersLink == source) {
			doOpenReminders(ureq);
		} else if(reportAccessLink == source) {
			doOpenReportAccess(ureq);
		} else if(reportLink == source) {
			doOpenReport(ureq);
		} else if(previousReportLink == source) {
			doOpenPreviousReport(ureq);
		} else if(followUpReportLink == source) {
			doOpenFollowUpReport(ureq);
		} else if (stackPanel == source && stackPanel.getLastController() == this && event instanceof PopEvent) {
			PopEvent popEvent = (PopEvent) event;
			if (popEvent.isClose()) {
				stackPanel.popController(this);
			} else {
				doOpenConfiguration(ureq);
			}
		}
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		doOpenConfiguration(ureq, false);
	}

	private void doOpenConfiguration(UserRequest ureq, boolean validate) {
		configurationCtrl = new DataCollectionConfigurationController(ureq, getWindowControl(), secCallback, stackPanel,
				dataCollection, validate);
		listenTo(configurationCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("data.collection.configuration"), configurationCtrl);
		segmentButtonsCmp.setSelectedButton(configurationLink);
	}
	
	private void doOpenParticipants(UserRequest ureq) {
		stackPanel.popUpToController(this);
		participationsCtrl = new ParticipationListController(ureq, getWindowControl(), secCallback, stackPanel,
				dataCollection);
		listenTo(participationsCtrl);
		stackPanel.pushController(translate("data.collection.participations"), participationsCtrl);
		segmentButtonsCmp.setSelectedButton(participantsLink);
	}
	
	private void doOpenReminders(UserRequest ureq) {
		stackPanel.popUpToController(this);
		remindersCtrl = new RemindersController(ureq, getWindowControl(), secCallback, dataCollection);
		listenTo(remindersCtrl);
		stackPanel.pushController(translate("data.collection.reminders"), remindersCtrl);
		segmentButtonsCmp.setSelectedButton(remindersLink);
	}
	
	private void doOpenReportAccess(UserRequest ureq) {
		stackPanel.popUpToController(this);
		reportAccessCtrl = new DataCollectionReportAccessController(ureq, getWindowControl(), secCallback, dataCollection);
		listenTo(reportAccessCtrl);
		stackPanel.pushController(translate("data.collection.report.access"), reportAccessCtrl);
		segmentButtonsCmp.setSelectedButton(reportAccessLink);
	}
	
	private void doOpenReport(UserRequest ureq) {
		stackPanel.popUpToController(this);
		reportCtrl = new DataCollectionReportController(ureq, getWindowControl(), dataCollection);
		listenTo(reportCtrl);
		stackPanel.pushController(translate("data.collection.report"), reportCtrl);
		segmentButtonsCmp.setSelectedButton(reportLink);
	}
	
	private void doOpenPreviousReport(UserRequest ureq) {
		stackPanel.popUpToController(this);
		previousReportCtrl = new DataCollectionReportController(ureq, getWindowControl(), previousDataCollection);
		listenTo(previousReportCtrl);
		stackPanel.pushController(translate("data.collection.report.previous"), previousReportCtrl);
		segmentButtonsCmp.setSelectedButton(previousReportLink);
	}
	
	private void doOpenFollowUpReport(UserRequest ureq) {
		stackPanel.popUpToController(this);
		followUpReportCtrl = new DataCollectionReportController(ureq, getWindowControl(), followUpDataCollection);
		listenTo(followUpReportCtrl);
		stackPanel.pushController(translate("data.collection.report.followup"), followUpReportCtrl);
		segmentButtonsCmp.setSelectedButton(followUpReportLink);
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
        super.doDispose();
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(finishConfirmationController);
		removeAsListenerAndDispose(startConfirmationController);
		removeAsListenerAndDispose(cmc);
		finishConfirmationController = null;
		startConfirmationController = null;
		cmc = null;
	}

	private void doSetStatusPreparation(UserRequest ureq) {
		doChangeStatus(ureq, PREPARATION);
	}

	private void doSetStatusReady(UserRequest ureq) {
		boolean allOk = true;
		allOk &= configurationCtrl.validateFormLogic(ureq);
		allOk &= configurationCtrl.validateExtendedFormLogic(true);
		if (allOk) {
			doChangeStatus(ureq, READY);
		} else {
			doOpenConfiguration(ureq, true);
		}
	}

	private void doConfirmStatusRunning(UserRequest ureq) {
		boolean allOk = true;
		allOk &= configurationCtrl.validateFormLogic(ureq);
		allOk &= configurationCtrl.validateExtendedFormLogic(true);
		if (allOk) {
			startConfirmationController = new DataCollectionStartConfirmationController(ureq, getWindowControl());
			this.listenTo(startConfirmationController);
	
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					startConfirmationController.getInitialComponent(), true,
					translate("data.collection.start.confirm.title"));
			cmc.activate();
		} else {
			doOpenConfiguration(ureq, true);
		}
	}

	protected void doSetStatusRunning(UserRequest ureq) {
		dataCollection.setStart(new Date());
		dataCollection = qualityService.updateDataCollection(dataCollection);
		doChangeStatus(ureq, RUNNING);
	}

	private void doConfirmStatusFinished(UserRequest ureq) {
		finishConfirmationController = new DataCollectionFinishConfirmationController(ureq, getWindowControl());
		this.listenTo(finishConfirmationController);
	
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				finishConfirmationController.getInitialComponent(), true,
				translate("data.collection.finish.confirm.title"));
		cmc.activate();
	}

	protected void doSetStatusFinished(UserRequest ureq) {
		dataCollection.setDeadline(new Date());
		dataCollection = qualityService.updateDataCollection(dataCollection);
		doChangeStatus(ureq, FINISHED);
	}

	private void doChangeStatus(UserRequest ureq, QualityDataCollectionStatus status) {
		dataCollection = qualityService.updateDataCollectionStatus(dataCollection, status);
		afterDataCollectionUpdated(ureq);
	}
	
	private void afterDataCollectionUpdated(UserRequest ureq) {
		secCallback = QualitySecurityCallbackFactory.createDataCollectionSecurityCallback(
				ureq.getUserSession().getRoles(), dataCollection, organisations);
		initTools();
		if (configurationCtrl != null) {
			configurationCtrl.onChanged(dataCollection, secCallback);
		}
		if (participationsCtrl != null) {
			participationsCtrl.onChanged(dataCollection, secCallback, ureq);
		}
		if (remindersCtrl != null) {
			remindersCtrl.onChanged(dataCollection, secCallback);
		}
		if (reportAccessCtrl != null) {
			reportAccessCtrl.onChanged(secCallback, ureq);
		}
		addReportButtons();
	}

}
