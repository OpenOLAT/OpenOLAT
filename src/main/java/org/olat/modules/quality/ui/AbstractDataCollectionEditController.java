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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.ui.event.DataCollectionEvent;
import org.olat.modules.quality.ui.event.DataCollectionEvent.Action;

/**
 * 
 * Initial date: 07.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractDataCollectionEditController extends FormBasicController implements TooledController {

	private Link statusPreparationLink;
	private Link statusReadyLink;
	private Link statusRunningLink;
	private Link statusFinishedLink;
	private Link deleteLink;
	
	protected final TooledStackedPanel stackPanel;
	protected final QualitySecurityCallback secCallback;
	protected QualityDataCollection dataCollection;
	
	AbstractDataCollectionEditController(UserRequest ureq, WindowControl wControl,
			QualitySecurityCallback secCallback, TooledStackedPanel stackPanel,
			QualityDataCollection dataCollection) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		this.dataCollection = dataCollection;
	}
	
	AbstractDataCollectionEditController(UserRequest ureq, WindowControl wControl,
			QualitySecurityCallback secCallback, TooledStackedPanel stackPanel,
			QualityDataCollection dataCollection, String layoutName) {
		super(ureq, wControl, layoutName);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		this.dataCollection = dataCollection;
	}
	
	AbstractDataCollectionEditController(UserRequest ureq, WindowControl wControl,
			QualitySecurityCallback secCallback, TooledStackedPanel stackPanel,
			QualityDataCollection dataCollection, int layout) {
		super(ureq, wControl, layout);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		this.dataCollection = dataCollection;
	}
	
	protected void setDataCollection(UserRequest ureq, QualityDataCollection dataCollection) {
		this.dataCollection = dataCollection;
		updateUI(ureq);
	}
	
	protected abstract void updateUI(UserRequest ureq);

	@Override
	public void initTools() {
		stackPanel.removeAllTools();
		initStatusTools();
		initButtons();
	}

	private void initStatusTools() {
		Component statusCmp;
		if (canChangeStatus()) {
			statusCmp = buildStatusDrowdown();
		} else {
			statusCmp = buildStatusLink();
		}
		stackPanel.addTool(statusCmp, Align.left);
	}

	private boolean canChangeStatus() {
		return secCallback.canSetPreparation(dataCollection)
				|| secCallback.canSetReady(dataCollection)
				|| secCallback.canSetRunning(dataCollection)
				|| secCallback.canSetFinished(dataCollection);
	}

	private Dropdown buildStatusDrowdown() {
		QualityDataCollectionStatus actualStatus = dataCollection.getStatus();
	
		Dropdown statusDropdown = new Dropdown("process.states", "data.collection.status." + actualStatus.name().toLowerCase(), false, getTranslator());
		statusDropdown.setElementCssClass("o_qual_tools_status o_qual_dc_status_" + actualStatus.name().toLowerCase());
		statusDropdown.setIconCSS("o_icon o_icon-fw o_icon_qual_dc_" + actualStatus.name().toLowerCase());
		statusDropdown.setOrientation(DropdownOrientation.normal);
	
		statusPreparationLink = LinkFactory.createToolLink("data.collection.status.preparation", translate("data.collection.status.preparation"), this);
		statusPreparationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_preparation");
		statusPreparationLink.setElementCssClass("o_labeled o_qual_status o_qual_dc_status_preparation");
		statusPreparationLink.setVisible(secCallback.canSetPreparation(dataCollection));
		statusDropdown.addComponent(statusPreparationLink);
	
		statusReadyLink = LinkFactory.createToolLink("data.collection.status.ready", translate("data.collection.status.ready"), this);
		statusReadyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_ready");
		statusReadyLink.setElementCssClass("o_labeled o_qual_status o_qual_dc_status_ready");
		statusReadyLink.setVisible(secCallback.canSetReady(dataCollection));
		statusDropdown.addComponent(statusReadyLink);
		
		statusRunningLink = LinkFactory.createToolLink("data.collection.status.running", translate("data.collection.status.running"), this);
		statusRunningLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_running");
		statusRunningLink.setElementCssClass("o_labeled o_qual_status o_qual_dc_status_running");
		statusRunningLink.setVisible(secCallback.canSetRunning(dataCollection));
		statusDropdown.addComponent(statusRunningLink);
		
		statusFinishedLink = LinkFactory.createToolLink("data.collection.status.finished", translate("data.collection.status.finished"), this);
		statusFinishedLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_finished");
		statusFinishedLink.setElementCssClass("o_labeled o_qual_status o_qual_dc_status_finished");
		statusFinishedLink.setVisible(secCallback.canSetFinished(dataCollection));
		statusDropdown.addComponent(statusFinishedLink);
		
		return statusDropdown;
	}

	private Component buildStatusLink() {
		QualityDataCollectionStatus actualStatus = dataCollection.getStatus();
		Link statusLink = LinkFactory.createToolLink("status.link",
				translate("data.collection.status." + actualStatus.name().toLowerCase()), this);
		statusLink.setElementCssClass("o_qual_tools_status o_qual_dc_status_" + actualStatus.name().toLowerCase());
		statusLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_" + actualStatus.name().toLowerCase());
		return statusLink;
	}

	private void initButtons() {
		if (secCallback.canDeleteDataCollection(dataCollection) ) {
			deleteLink = LinkFactory.createToolLink("data.collection.delete", translate("data.collection.delete"),
					this);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_dc_delete");
			stackPanel.addTool(deleteLink, Align.left);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == statusPreparationLink) {
			fireEvent(ureq, new DataCollectionEvent(dataCollection, Action.STATUS_PREPARATION_SELECTED));
		} else if (source == statusReadyLink) {
			fireEvent(ureq, new DataCollectionEvent(dataCollection, Action.STATUS_READY_SELECTED));
		} else if (source == statusRunningLink) {
			fireEvent(ureq, new DataCollectionEvent(dataCollection, Action.STATUS_RUNNING_SELECTED));
		} else if (source == statusFinishedLink) {
			fireEvent(ureq, new DataCollectionEvent(dataCollection, Action.STATUS_FINISHED_SELECTED));
		} else if (source == deleteLink) {
			fireEvent(ureq, new DataCollectionEvent(dataCollection, Action.DELETE));
		}
		super.event(ureq, source, event);
	}

}